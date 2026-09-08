# 使用JDBC核心类控制基本JDBC处理和错误处理

本节介绍如何使用 JDBC 核心类来控制基本的 JDBC 处理，包括错误处理。它包含以下主题：

- 使用 JdbcTemplate

# 使用 JdbcTemplate

JdbcTemplate 是 JDBC 核心包中的核心类。它负责资源的创建和释放，从而帮助您避免常见的错误（例如忘记关闭连接）。它执行核心 JDBC 工作流的基本任务（例如语句的创建和执行），而应用程序代码只需提供 SQL 语句并提取结果。JdbcTemplate 类具有以下功能：

运行 SQL 查询 
更新语句和调用存储过程 
对 ResultSet 实例进行迭代，并提取返回的参数值 
捕获 JDBC 异常，并将其转换为 org.springframework.dao 包中定义的通用且更具描述性的异常体系结构。（参见“一致的异常体系结构”）

当您在代码中使用 JdbcTemplate 时，只需实现回调接口，这些接口具有明确定义的契约。在 JdbcTemplate 类提供的 Connection 的基础上，PreparedStatementCreator 回调接口可以创建预处理语句，并提供 SQL 语句和所有必要的参数。CallableStatementCreator 接口也是如此，它用于创建可调用语句。RowCallbackHandler 接口则用于从 ResultSet 的每一行中提取值。

您可以在 DAO 实现中使用 JdbcTemplate，通过直接实例化并传入 DataSource 引用，或者将其配置在 Spring IoC 容器中，并以 bean 的形式提供给 DAO。


数据源（DataSource）应始终在Spring IoC容器中配置为Bean。第一种情况是将Bean直接提供给服务；第二种情况是将Bean提供给预准备模板。

该类发出的所有SQL语句都会以DEBUG级别记录在模板实例的完全限定类名对应的类别下（通常是JdbcTemplate，但如果您使用了JdbcTemplate类的自定义子类，则可能不同）。
以下部分提供了一些JdbcTemplate的使用示例。这些示例并非JdbcTemplate所暴露的所有功能的详尽列表。如需了解完整功能，请参见附带的javadoc文档。

## Querying (SELECT)

```java

int rowCount = this.jdbcTemplate.queryForObject("select count(*) from t_actor", Integer.class);

int countOfActorsNamedJoe = this.jdbcTemplate.queryForObject(
		"select count(*) from t_actor where first_name = ?", Integer.class, "Joe");

String lastName = this.jdbcTemplate.queryForObject(
		"select last_name from t_actor where id = ?",
		String.class, 1212L);

Actor actor = jdbcTemplate.queryForObject(
		"select first_name, last_name from t_actor where id = ?",
		(resultSet, rowNum) -> {
			Actor newActor = new Actor();
			newActor.setFirstName(resultSet.getString("first_name"));
			newActor.setLastName(resultSet.getString("last_name"));
			return newActor;
		},
		1212L);

List<Actor> actors = this.jdbcTemplate.query(
		"select first_name, last_name from t_actor",
		(resultSet, rowNum) -> {
			Actor actor = new Actor();
			actor.setFirstName(resultSet.getString("first_name"));
			actor.setLastName(resultSet.getString("last_name"));
			return actor;
		});
```
如果最后两段代码片段确实存在于同一个应用程序中，那么消除两个 RowMapper lambda 表达式中存在的重复代码，并将它们提取到一个单独的字段中，以便 DAO 方法在需要时引用，这是有意义的。例如，将前面的代码片段改写为如下形式可能更好：

```java

private final RowMapper<Actor> actorRowMapper = (resultSet, rowNum) -> {
	Actor actor = new Actor();
	actor.setFirstName(resultSet.getString("first_name"));
	actor.setLastName(resultSet.getString("last_name"));
	return actor;
};

public List<Actor> findAllActors() {
	return this.jdbcTemplate.query("select first_name, last_name from t_actor", actorRowMapper);
}
```
## Updating (INSERT, UPDATE, and DELETE) with JdbcTemplate

你可以使用 update(..) 方法来执行插入、更新和删除操作。参数值通常以可变参数的形式提供，或者也可以作为对象数组提供。
```java


this.jdbcTemplate.update(
		"insert into t_actor (first_name, last_name) values (?, ?)",
		"Leonor", "Watling");

this.jdbcTemplate.update(
		"update t_actor set last_name = ? where id = ?",
		"Banjo", 5276L);

this.jdbcTemplate.update(
		"delete from t_actor where id = ?",
		Long.valueOf(actorId));


```

## Other JdbcTemplate Operations

你可以使用 execute(..) 方法来运行任意 SQL。因此，该方法通常用于 DDL 语句。它被大量重载，提供了多种变体，可以接受回调接口、绑定变量数组等。以下示例创建了一个表：

```java

this.jdbcTemplate.execute("create table mytable (id integer, name varchar(100))");

this.jdbcTemplate.update(
		"call SUPPORT.REFRESH_ACTORS_SUMMARY(?)",
		Long.valueOf(unionId));
```

## JdbcTemplate Best Practices

JdbcTemplate 类的实例在配置后是线程安全的。这一点很重要，因为它意味着您可以配置一个 JdbcTemplate 实例，然后将这个共享的引用安全地注入到多个 DAO（或存储库）中。JdbcTemplate 是有状态的，因为它维护着一个对 DataSource 的引用，但这种状态不是会话状态。

在使用 JdbcTemplate 类（以及相关的 NamedParameterJdbcTemplate 类）时，一种常见的做法是在 Spring 配置文件中配置一个 DataSource，然后将这个共享的 DataSource bean 依赖注入到您的 DAO 类中。JdbcTemplate 是在 DataSource 的 setter 方法中或在构造函数中创建的。这将导致 DAO 类如下所示：

```java

public class JdbcCorporateEventDao implements CorporateEventDao {

	private final JdbcTemplate jdbcTemplate;

	public JdbcCorporateEventDao(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	// JDBC-backed implementations of the methods on the CorporateEventDao follow...
}
```

```java

@Bean
JdbcCorporateEventDao corporateEventDao(DataSource dataSource) {
	return new JdbcCorporateEventDao(dataSource);
}

@Bean(destroyMethod = "close")
BasicDataSource dataSource() {
	BasicDataSource dataSource = new BasicDataSource();
	dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
	dataSource.setUrl("jdbc:hsqldb:hsql://localhost:");
	dataSource.setUsername("sa");
	dataSource.setPassword("");
	return dataSource;
}
```

```xml
<bean id="corporateEventDao" class="org.example.jdbc.JdbcCorporateEventDao">
	<constructor-arg ref="dataSource"/>
</bean>

<bean id="dataSource" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
	<property name="driverClassName" value="${jdbc.driverClassName}"/>
	<property name="url" value="${jdbc.url}"/>
	<property name="username" value="${jdbc.username}"/>
	<property name="password" value="${jdbc.password}"/>
</bean>

<context:property-placeholder location="jdbc.properties"/>

```

显式配置的一种替代方案是使用组件扫描和依赖注入的注解支持。在这种情况下，您可以使用 @Repository 注解标记该类（使其成为组件扫描的候选对象）。以下示例展示了如何实现这一点：

```java
@Repository
public class JdbcCorporateEventRepository implements CorporateEventRepository {

	private JdbcTemplate jdbcTemplate;

	// Implicitly autowire the DataSource constructor parameter
	public JdbcCorporateEventRepository(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	// JDBC-backed implementations of the methods on the CorporateEventRepository follow...
}

```
如果你使用 Spring 的 JdbcDaoSupport 类，并且你的各种基于 JDBC 的 DAO 类都继承自它，那么你的子类将从 JdbcDaoSupport 类继承一个 setDataSource(..) 方法。你可以选择是否继承这个类。JdbcDaoSupport 类的提供仅是为了方便。

无论你选择使用（或不使用）上述哪种模板初始化方式，通常都不需要每次运行 SQL 时都创建一个新的 JdbcTemplate 类实例。一旦配置完成，JdbcTemplate 实例是线程安全的。如果你的应用程序访问多个数据库，你可能需要多个 JdbcTemplate 实例，这需要多个 DataSource，进而需要多个配置不同的 JdbcTemplate 实例。

# Using NamedParameterJdbcTemplate

NamedParameterJdbcTemplate 类支持通过使用命名参数来编程 JDBC 语句，而不是仅使用经典的占位符（'？'）参数来编程 JDBC 语句。NamedParameterJdbcTemplate 类封装了一个 JdbcTemplate，并将大部分工作委托给被包装的 JdbcTemplate。本节仅介绍 NamedParameterJdbcTemplate 类中与 JdbcTemplate 本身不同的部分——即通过使用命名参数来编程 JDBC 语句。以下示例展示了如何使用 NamedParameterJdbcTemplate：

```java

// some JDBC-backed DAO class...
private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

public void setDataSource(DataSource dataSource) {
	this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
}

public int countOfActorsByFirstName(String firstName) {
	String sql = "select count(*) from t_actor where first_name = :first_name";
	SqlParameterSource namedParameters = new MapSqlParameterSource("first_name", firstName);
	return this.namedParameterJdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
}
```

注意在分配给 sql 变量的值中使用了命名参数表示法，以及插入到 namedParameters 变量（类型为 MapSqlParameterSource）中的相应值。 
或者，您可以使用基于 Map 的方式将命名参数及其对应值传递给 NamedParameterJdbcTemplate 实例。 
NamedParameterJdbcOperations 接口中定义并由 NamedParameterJdbcTemplate 类实现的其他方法遵循类似模式，此处不再详述。 
以下示例展示了基于 Map 方式的使用：

```java

// some JDBC-backed DAO class...
private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

public void setDataSource(DataSource dataSource) {
	this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
}

public int countOfActorsByFirstName(String firstName) {
	String sql = "select count(*) from t_actor where first_name = :first_name";
	Map<String, String> namedParameters = Collections.singletonMap("first_name", firstName);
	return this.namedParameterJdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
}
```

与 NamedParameterJdbcTemplate（以及存在于同一 Java 包中）相关的一个不错特性是 SqlParameterSource 接口。在前面的代码片段中，你已经看到过该接口的一个实现示例（MapSqlParameterSource 类）。SqlParameterSource 是一个为 NamedParameterJdbcTemplate 提供命名参数值的来源。MapSqlParameterSource 类是一个简单的实现，它是一个围绕 java.util.Map 的适配器，其中键是参数名称，值是参数值。

另一个 SqlParameterSource 的实现是 BeanPropertySqlParameterSource 类。该类包装了一个任意的 JavaBean（即一个遵循 JavaBean 规范的类的实例），并使用被包装的 JavaBean 的属性作为命名参数值的来源。

以下示例展示了一个典型的 JavaBean：

```java

public class Actor {

	private Long id;
	private String firstName;
	private String lastName;

	public String getFirstName() {
		return this.firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public Long getId() {
		return this.id;
	}

	// setters omitted...
}
```
以下示例使用 NamedParameterJdbcTemplate 来返回前一个示例中所示类的成员数量：

```java


// some JDBC-backed DAO class...
private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

public void setDataSource(DataSource dataSource) {
	this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
}

public int countOfActors(Actor exampleActor) {
	// notice how the named parameters match the properties of the above 'Actor' class
	String sql = "select count(*) from t_actor where first_name = :firstName and last_name = :lastName";
	SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(exampleActor);
	return this.namedParameterJdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
}
```

请记住，NamedParameterJdbcTemplate 类封装了一个经典的 JdbcTemplate 模板。如果您需要访问被封装的 JdbcTemplate 实例以使用仅存在于 JdbcTemplate 类中的功能，您可以使用 getJdbcOperations() 方法，通过 JdbcOperations 接口访问被封装的 JdbcTemplate。

另请参阅 JdbcTemplate 最佳实践，以获取在应用程序上下文中使用 NamedParameterJdbcTemplate 类的指南。

## 统一的 JDBC 查询/更新操作：JdbcClient

截至6.1版本，NamedParameterJdbcTemplate的命名参数语句和普通JdbcTemplate的位置参数语句可通过统一的客户端API以流畅的交互模型使用。
例如，使用位置参数时：
```java

private JdbcClient jdbcClient = JdbcClient.create(dataSource);

public int countOfActorsByFirstName(String firstName) {
	return this.jdbcClient.sql("select count(*) from t_actor where first_name = ?")
			.param(firstName)
			.query(Integer.class).single();
}
```

```java
private JdbcClient jdbcClient = JdbcClient.create(dataSource);

public int countOfActorsByFirstName(String firstName) {
	return this.jdbcClient.sql("select count(*) from t_actor where first_name = :firstName")
			.param("firstName", firstName)
			.query(Integer.class).single();
}
```
```java
List<Actor> actors = this.jdbcClient.sql("select first_name, last_name from t_actor")
		.query((rs, rowNum) -> new Actor(rs.getString("first_name"), rs.getString("last_name")))
		.list();
```
RowMapper 功能同样可用，具有灵活的结果解析能力：

```java

List<Actor> actors = this.jdbcClient.sql("select first_name, last_name from t_actor")
		.query((rs, rowNum) -> new Actor(rs.getString("first_name"), rs.getString("last_name")))
		.list();
```
除了自定义的 RowMapper，您还可以指定一个要映射到的类。例如，假设 Actor 是一个记录类，具有 firstName 和 lastName 属性，您可以采用自定义构造函数、bean 属性或普通字段的方式：

```java

List<Actor> actors = this.jdbcClient.sql("select first_name, last_name from t_actor")
		.query(Actor.class)
		.list();
```

```java
Actor actor = this.jdbcClient.sql("select first_name, last_name from t_actor where id = ?")
		.param(1212L)
		.query(Actor.class)
		.single();
```
```java
Optional<Actor> actor = this.jdbcClient.sql("select first_name, last_name from t_actor where id = ?")
		.param(1212L)
		.query(Actor.class)
		.optional();
```

```java
this.jdbcClient.sql("insert into t_actor (first_name, last_name) values (?, ?)")
		.param("Leonor").param("Watling")
		.update();
```

```java
this.jdbcClient.sql("insert into t_actor (first_name, last_name) values (:firstName, :lastName)")
		.param("firstName", "Leonor").param("lastName", "Watling")
		.update();
```
除了单独的命名参数外，您还可以指定一个参数源对象——例如，一个记录类、一个具有 bean 属性的类，或一个提供 firstName 和 lastName 属性的普通字段持有者，比如上面提到的 Actor 类。
```java
this.jdbcClient.sql("insert into t_actor (first_name, last_name) values (:firstName, :lastName)")
		.paramSource(new Actor("Leonor", "Watling"))
		.update();
```
参数的自动 Actor 类映射以及上述查询结果是通过隐式的 SimplePropertySqlParameterSource 和 SimplePropertyRowMapper 策略提供的，这些策略也可以直接使用。它们可以作为 BeanPropertySqlParameterSource 和 BeanPropertyRowMapper/DataClassRowMapper 的通用替代方案，同样适用于 JdbcTemplate 和 NamedParameterJdbcTemplate 本身。

JdbcClient 是一个灵活但简化的 JDBC 查询/更新语句封装层。高级功能（如批量插入和存储过程调用）通常需要额外的定制：对于 JdbcClient 中未提供的此类功能，可以考虑使用 Spring 的 SimpleJdbcInsert 和 SimpleJdbcCall 类，或者直接使用 JdbcTemplate。

# SQLExceptionTranslator
SQLExceptionTranslator 是一个接口，由能够将 SQLException 与 Spring 自身的 org.springframework.dao.DataAccessException 相互转换的类实现，该异常与数据访问策略无关。实现可以是通用的（例如，使用 JDBC 的 SQLState 代码），也可以是专有的（例如，使用 Oracle 错误代码）以获得更高的精确度。这种异常转换机制在通用的 JdbcTemplate 和 JdbcTransactionManager 入口点背后使用，这些入口点不会传播 SQLException，而是传播 DataAccessException。

从6.0版本开始，默认异常转换器为`SQLExceptionSubclassTranslator`，它会检测JDBC 4的`SQLException`子类，并附带一些额外的检查，如果检测失败，则回退到通过`SQLStateSQLExceptionTranslator`进行SQL状态码的推断。对于常见的数据库访问操作，这通常已经足够，不需要进行特定厂商的检测。为了向后兼容，可以考虑使用下文介绍的`SQLErrorCodeSQLExceptionTranslator`，并且可以结合自定义的错误码映射。

`SQLErrorCodeSQLExceptionTranslator`是`SQLExceptionTranslator`的实现类，当类路径根目录下存在名为`sql-error-codes.xml`的文件时，它会被默认使用。该实现使用了特定厂商的错误码，因此比SQL状态码或`SQLException`子类转换更为精确。错误码的转换基于一个名为`SQLErrorCodes`的JavaBean类型类中保存的错误码。该类由`SQLErrorCodesFactory`创建并填充，而`SQLErrorCodesFactory`（顾名思义）是一个工厂类，用于根据名为`sql-error-codes.xml`的配置文件内容创建`SQLErrorCodes`。该文件会填充厂商的错误码，并基于从`DatabaseMetaData`中获取的`DatabaseProductName`。实际使用的数据库的错误码会被使用。

`SQLErrorCodeSQLExceptionTranslator`按照以下顺序应用匹配规则：

1. 任何由子类实现的自定义转换。通常情况下，使用的是提供的具体`SQLErrorCodeSQLExceptionTranslator`实现，因此该规则不适用。只有在你确实提供了子类实现时，该规则才适用。
2. 任何作为`SQLErrorCodes`类的`customSqlExceptionTranslator`属性提供的`SQLExceptionTranslator`接口的自定义实现。
3. 搜索`CustomSQLErrorCodesTranslation`类的实例列表（该列表由`SQLErrorCodes`类的`customTranslations`属性提供），以寻找匹配项。
4. 应用错误码匹配。
5. 使用备用转换器。`SQLExceptionSubclassTranslator`是默认的备用转换器。如果该转换器不可用，则下一个备用转换器是`SQLStateSQLExceptionTranslator`。


默认情况下，SQLErrorCodesFactory 用于定义错误代码和自定义异常转换。这些错误代码和转换规则会在类路径中名为 sql-error-codes.xml 的文件中查找，并根据当前使用数据库的数据库元数据中的数据库名称，找到匹配的 SQLErrorCodes 实例。

您可以扩展 SQLErrorCodeSQLExceptionTranslator，如下例所示：

```java
public class CustomSQLErrorCodesTranslator extends SQLErrorCodeSQLExceptionTranslator {

	protected DataAccessException customTranslate(String task, String sql, SQLException sqlEx) {
		if (sqlEx.getErrorCode() == -12345) {
			return new DeadlockLoserDataAccessException(task, sqlEx);
		}
		return null;
	}
}
```
在前面的示例中，特定的错误代码（-12345）被翻译了，而其他错误则由默认的错误翻译器实现进行翻译。要使用这个自定义的翻译器，必须通过 `setExceptionTranslator` 方法将其传递给 `JdbcTemplate`，并且在使用该翻译器所需的所有数据访问处理过程中，都必须使用这个 `JdbcTemplate`。以下示例展示了如何使用这个自定义翻译器：


```java
private JdbcTemplate jdbcTemplate;

public void setDataSource(DataSource dataSource) {
	// create a JdbcTemplate and set data source
	this.jdbcTemplate = new JdbcTemplate();
	this.jdbcTemplate.setDataSource(dataSource);

	// create a custom translator and set the DataSource for the default translation lookup
	CustomSQLErrorCodesTranslator tr = new CustomSQLErrorCodesTranslator();
	tr.setDataSource(dataSource);
	this.jdbcTemplate.setExceptionTranslator(tr);
}

public void updateShippingCharge(long orderId, long pct) {
	// use the prepared JdbcTemplate for this update
	this.jdbcTemplate.update("update orders" +
		" set shipping_charge = shipping_charge * ? / 100" +
		" where id = ?", pct, orderId);
}
```
自定义翻译器会接收一个数据源，以便在 sql-error-codes.xml 中查找错误代码。

# Running Statements

运行一个SQL语句所需的代码非常少。你只需要一个DataSource和一个JdbcTemplate，以及JdbcTemplate提供的便捷方法。以下示例展示了一个最小化但功能完整的类所需包含的内容，该类用于创建一个新表：
```java
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class ExecuteAStatement {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void doExecute() {
		this.jdbcTemplate.execute("create table mytable (id integer, name varchar(100))");
	}
}
```
# Running Queries
某些查询方法会返回单个值。要从一行中获取计数或特定值，请使用 `queryForObject(..)`。该方法会将返回的 JDBC 类型转换为作为参数传入的 Java 类。如果类型转换无效，则会抛出 `InvalidDataAccessApiUsageException`。以下示例包含两个查询方法，一个用于查询 `int` 类型，另一个用于查询 `String` 类型：

```java
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class RunAQuery {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public int getCount() {
		return this.jdbcTemplate.queryForObject("select count(*) from mytable", Integer.class);
	}

	public String getName() {
		return this.jdbcTemplate.queryForObject("select name from mytable", String.class);
	}
}
```

除了单条结果查询方法外，还有几种方法会返回一个列表，其中每个条目对应查询返回的一行数据。最通用的方法是 `queryForList(..)`，它返回一个 `List`，其中每个元素都是一个 `Map`，该 `Map` 包含每个列的一个条目，并以列名作为键。如果你在前面的示例中添加一个方法来检索所有行的列表，它可能如下所示：

```java
private JdbcTemplate jdbcTemplate;

public void setDataSource(DataSource dataSource) {
	this.jdbcTemplate = new JdbcTemplate(dataSource);
}

public List<Map<String, Object>> getList() {
	return this.jdbcTemplate.queryForList("select * from mytable");
}
```
结果是:
```java
[{name=Bob, id=1}, {name=Mary, id=2}]
```

# Updating the Database

以下示例更新了某个主键对应的列：

```java

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class ExecuteAnUpdate {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setName(int id, String name) {
		this.jdbcTemplate.update("update mytable set name = ? where id = ?", name, id);
	}
}
```
在前面的示例中，SQL语句中包含行参数的占位符。您可以将参数值作为可变参数传递，或者作为对象数组传递。因此，您应该显式地将基本类型包装在基本类型包装类中，或者使用自动装箱。


# Retrieving Auto-generated Keys

一个 `update()` 便利方法支持获取数据库生成的键值。此支持是 JDBC 3.0 标准的一部分。详情请参见规范的第 13.6 章。该方法以 `PreparedStatementCreator` 作为第一个参数，通过这种方式指定所需的插入语句。另一个参数是 `KeyHolder`，它会在更新成功返回时包含生成的键值。由于没有创建合适的 `PreparedStatement` 的标准单一方式（这也解释了为什么方法签名是这样的），因此存在多种可能性。以下示例在 Oracle 上可以运行，但在其他平台上可能无法运行：


```java
final String INSERT_SQL = "insert into my_test (name) values(?)";
final String name = "Rob";

KeyHolder keyHolder = new GeneratedKeyHolder();
jdbcTemplate.update(connection -> {
	PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[] { "id" });
	ps.setString(1, name);
	return ps;
}, keyHolder);

// keyHolder.getKey() now contains the generated key
```