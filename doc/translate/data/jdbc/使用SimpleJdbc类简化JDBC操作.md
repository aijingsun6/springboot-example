# 使用 SimpleJdbc 类简化 JDBC 操作

SimpleJdbcInsert 和 SimpleJdbcCall 类通过利用可通过 JDBC 驱动程序检索的数据库元数据，提供了简化的配置。这意味着您无需在前期进行过多配置，不过如果您更倾向于在代码中提供所有详细信息，也可以覆盖或关闭元数据处理

# 使用 SimpleJdbcInsert 插入数据

我们首先来看 `SimpleJdbcInsert` 类，它具有最少的配置选项。你应该在数据访问层的初始化方法中实例化 `SimpleJdbcInsert`。在本示例中，初始化方法是 `setDataSource` 方法。你不需要继承 `SimpleJdbcInsert` 类。相反，你可以创建一个新实例，并使用 `withTableName` 方法设置表名。该类的配置方法遵循流畅风格，会返回 `SimpleJdbcInsert` 的实例，从而允许你将所有配置方法链式调用。以下示例仅使用了一个配置方法（稍后我们会展示多个方法的示例）：

```java

public class JdbcActorDao implements ActorDao {

	private SimpleJdbcInsert insertActor;

	public void setDataSource(DataSource dataSource) {
		this.insertActor = new SimpleJdbcInsert(dataSource).withTableName("t_actor");
	}

	public void add(Actor actor) {
		Map<String, Object> parameters = new HashMap<>(3);
		parameters.put("id", actor.getId());
		parameters.put("first_name", actor.getFirstName());
		parameters.put("last_name", actor.getLastName());
		insertActor.execute(parameters);
	}

	// ... additional methods
}
```

此处使用的 execute 方法以一个普通的 java.util.Map 作为其唯一参数。需要注意的是，Map 中使用的键必须与数据库中定义的表的列名相匹配。这是因为我们读取元数据来构建实际的插入语句。

# 使用 SimpleJdbcInsert 检索自动生成的主键

下一个示例使用了与前一个示例相同的插入操作，但与直接传入id不同，它获取了自动生成的键并将其设置到新的Actor对象上。在创建SimpleJdbcInsert时，除了指定表名之外，还通过usingGeneratedKeyColumns方法指定了生成键列的名称。以下代码清单展示了其工作原理：

```java

public class JdbcActorDao implements ActorDao {

	private SimpleJdbcInsert insertActor;

	public void setDataSource(DataSource dataSource) {
		this.insertActor = new SimpleJdbcInsert(dataSource)
				.withTableName("t_actor")
				.usingGeneratedKeyColumns("id");
	}

	public void add(Actor actor) {
		Map<String, Object> parameters = new HashMap<>(2);
		parameters.put("first_name", actor.getFirstName());
		parameters.put("last_name", actor.getLastName());
		Number newId = insertActor.executeAndReturnKey(parameters);
		actor.setId(newId.longValue());
	}

	// ... additional methods
}
```

使用第二种方式运行插入操作时，主要区别在于你不将 id 添加到 Map 中，而是调用 executeAndReturnKey 方法。该方法会返回一个 java.lang.Number 对象，你可以用它来创建域类中使用的数值类型的实例。你不能依赖所有数据库在此处返回特定的 Java 类。java.lang.Number 是你可以依赖的基类。如果你有多个自动生成的列，或者生成的值是非数值类型，你可以使用 executeAndReturnKeyHolder 方法返回的 KeyHolder。


# 为 SimpleJdbcInsert 指定列

你可以通过使用 usingColumns 方法指定列名列表来限制插入的列，如下例所示：

```java

public class JdbcActorDao implements ActorDao {

	private SimpleJdbcInsert insertActor;

	public void setDataSource(DataSource dataSource) {
		this.insertActor = new SimpleJdbcInsert(dataSource)
				.withTableName("t_actor")
				.usingColumns("first_name", "last_name")
				.usingGeneratedKeyColumns("id");
	}

	public void add(Actor actor) {
		Map<String, Object> parameters = new HashMap<>(2);
		parameters.put("first_name", actor.getFirstName());
		parameters.put("last_name", actor.getLastName());
		Number newId = insertActor.executeAndReturnKey(parameters);
		actor.setId(newId.longValue());
	}

	// ... additional methods
}
```

插入操作的执行方式与依赖元数据来确定使用哪些列的方式相同。


# 使用 SqlParameterSource 提供参数值

使用 Map 来提供参数值是可以的，但它并不是最方便的类。Spring 提供了几种 SqlParameterSource 接口的实现，你可以使用这些实现来替代。第一个实现是 BeanPropertySqlParameterSource，如果你有一个符合 JavaBean 规范的类来保存你的值，那么这个类就非常方便。它会使用相应的 getter 方法来提取参数值。以下示例展示了如何使用 BeanPropertySqlParameterSource：

```java
public class JdbcActorDao implements ActorDao {

	private SimpleJdbcInsert insertActor;

	public void setDataSource(DataSource dataSource) {
		this.insertActor = new SimpleJdbcInsert(dataSource)
				.withTableName("t_actor")
				.usingGeneratedKeyColumns("id");
	}

	public void add(Actor actor) {
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(actor);
		Number newId = insertActor.executeAndReturnKey(parameters);
		actor.setId(newId.longValue());
	}

	// ... additional methods
}

```
另一种选择是 `MapSqlParameterSource`，它类似于 `Map`，但提供了一个更方便的 `addValue` 方法，可以链式调用。以下示例展示了如何使用它：

```java
public class JdbcActorDao implements ActorDao {

	private SimpleJdbcInsert insertActor;

	public void setDataSource(DataSource dataSource) {
		this.insertActor = new SimpleJdbcInsert(dataSource)
				.withTableName("t_actor")
				.usingGeneratedKeyColumns("id");
	}

	public void add(Actor actor) {
		SqlParameterSource parameters = new MapSqlParameterSource()
				.addValue("first_name", actor.getFirstName())
				.addValue("last_name", actor.getLastName());
		Number newId = insertActor.executeAndReturnKey(parameters);
		actor.setId(newId.longValue());
	}

	// ... additional methods
}
```

正如你所看到的，配置是相同的。只需要更改执行代码即可使用这些替代的输入类。

# 使用 SimpleJdbcCall 调用存储过程

SimpleJdbcCall 类利用数据库中的元数据来查找输入和输出参数的名称，因此您无需显式声明它们。如果您更倾向于这样做，或者您有无法自动映射到 Java 类的参数，也可以显式声明参数。第一个示例展示了一个简单的存储过程，该过程仅从 MySQL 数据库中返回 VARCHAR 和 DATE 格式的标量值。该示例存储过程读取指定的演员条目，并以输出参数的形式返回 first_name、last_name 和 birth_date 列。以下代码清单展示了第一个示例：

```java

CREATE PROCEDURE read_actor (
	IN in_id INTEGER,
	OUT out_first_name VARCHAR(100),
	OUT out_last_name VARCHAR(100),
	OUT out_birth_date DATE)
BEGIN
	SELECT first_name, last_name, birth_date
	INTO out_first_name, out_last_name, out_birth_date
	FROM t_actor where id = in_id;
END;
```

`in_id` 参数包含您要查找的演员的 ID。`out` 参数返回从表中读取的数据。

您可以像声明 `SimpleJdbcInsert` 一样声明 `SimpleJdbcCall`。您应该在数据访问层的初始化方法中实例化并配置该类。与 `StoredProcedure` 类不同，您不需要创建子类，也不需要声明可以在数据库元数据中查找的参数。以下 `SimpleJdbcCall` 配置示例使用了前面的存储过程。除了 `DataSource` 之外，唯一的配置选项就是存储过程的名称。

```java
public class JdbcActorDao implements ActorDao {

	private SimpleJdbcCall procReadActor;

	public void setDataSource(DataSource dataSource) {
		this.procReadActor = new SimpleJdbcCall(dataSource)
				.withProcedureName("read_actor");
	}

	public Actor readActor(Long id) {
		SqlParameterSource in = new MapSqlParameterSource()
				.addValue("in_id", id);
		Map out = procReadActor.execute(in);
		Actor actor = new Actor();
		actor.setId(id);
		actor.setFirstName((String) out.get("out_first_name"));
		actor.setLastName((String) out.get("out_last_name"));
		actor.setBirthDate((Date) out.get("out_birth_date"));
		return actor;
	}

	// ... additional methods
}
```
你编写的用于执行调用的代码需要创建一个包含 IN 参数的 SqlParameterSource。你必须将为输入值提供的名称与存储过程中声明的参数名称相匹配。名称的大小写不必匹配，因为你可以使用元数据来确定在存储过程中如何引用数据库对象。在存储过程源代码中指定的内容，并不一定就是它在数据库中存储的方式。有些数据库会将名称转换为全大写，而另一些数据库则使用小写或按照指定的格式保留大小写。

execute 方法接收 IN 参数，并返回一个 Map，其中包含以存储过程中指定的名称为键的任何 OUT 参数。在本例中，这些参数是 out_first_name、out_last_name 和 out_birth_date。

execute 方法的最后一部分会创建一个 Actor 实例，用于返回检索到的数据。同样，使用存储过程中声明的 OUT 参数名称非常重要。此外，存储在结果 Map 中的 OUT 参数名称的大小写与数据库中 OUT 参数名称的大小写相匹配，而不同数据库之间的大小写规则可能有所不同。为了使代码更具可移植性，你应该执行不区分大小写的查找，或者指示 Spring 使用 LinkedCaseInsensitiveMap。要实现后者，你可以创建自己的 JdbcTemplate，并将 setResultsMapCaseInsensitive 属性设置为 true。然后，你可以将这个自定义的 JdbcTemplate 实例传递给 SimpleJdbcCall 的构造函数。以下示例展示了这种配置：


```java

public class JdbcActorDao implements ActorDao {

	private SimpleJdbcCall procReadActor;

	public void setDataSource(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.setResultsMapCaseInsensitive(true);
		this.procReadActor = new SimpleJdbcCall(jdbcTemplate)
				.withProcedureName("read_actor");
	}

	// ... additional methods
}
```

通过采取此操作，可以避免在用于返回的输出参数名称的情况下发生冲突。

# 显式声明用于 SimpleJdbcCall 的参数

在本章的前面部分，我们描述了如何从元数据中推断参数，但如果您愿意，也可以显式声明它们。您可以通过创建并配置 `SimpleJdbcCall` 来实现这一点，该方法使用 `declareParameters` 作为参数，该参数接受数量不定的 `SqlParameter` 对象作为输入。有关如何定义 `SqlParameter` 的详细信息，请参阅下一节。

如果您使用的数据库不是 Spring 支持的数据库，那么显式声明参数是必要的。目前，Spring 支持以下数据库存储过程调用的元数据查找：Apache Derby、DB2、MySQL、Microsoft SQL Server、Oracle 和 Sybase。我们还支持 MySQL、Microsoft SQL Server 和 Oracle 的存储函数元数据查找。

您可以选择显式声明一个、部分或全部参数。在您没有显式声明参数的地方，仍然会使用参数元数据。要绕过对潜在参数的所有元数据查找处理，仅使用已声明的参数，您可以在声明过程中调用 `withoutProcedureColumnMetaDataAccess` 方法。假设您为某个数据库函数声明了两个或多个不同的调用签名。在这种情况下，您可以调用 `useInParameterNames` 方法来指定要包含在给定签名中的 IN 参数名称列表。

以下示例展示了一个完全声明的存储过程调用，并使用了前面示例中的信息：
```java
public class JdbcActorDao implements ActorDao {

	private SimpleJdbcCall procReadActor;

	public void setDataSource(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.setResultsMapCaseInsensitive(true);
		this.procReadActor = new SimpleJdbcCall(jdbcTemplate)
				.withProcedureName("read_actor")
				.withoutProcedureColumnMetaDataAccess()
				.useInParameterNames("in_id")
				.declareParameters(
						new SqlParameter("in_id", Types.NUMERIC),
						new SqlOutParameter("out_first_name", Types.VARCHAR),
						new SqlOutParameter("out_last_name", Types.VARCHAR),
						new SqlOutParameter("out_birth_date", Types.DATE)
				);
	}

	// ... additional methods
}
```
两个示例的执行过程和最终结果相同。第二个示例明确指定了所有细节，而不是依赖元数据。

# 如何定义SqlParameters

要为 SimpleJdbc 类以及 RDBMS 操作类（在“将 JDBC 操作建模为 Java 对象”一节中介绍）定义参数，可以使用 SqlParameter 或其子类之一。为此，通常需要在构造函数中指定参数名称和 SQL 类型。SQL 类型通过使用 java.sql.Types 常量来指定。在本章前面部分，我们曾看到过如下类似的声明：
```java
new SqlParameter("in_id", Types.NUMERIC),
new SqlOutParameter("out_first_name", Types.VARCHAR),
```

第一行代码使用 `SqlParameter` 声明了一个输入参数（IN 参数）。您可以在存储过程调用以及查询中使用输入参数，具体方式是通过 `SqlQuery` 及其子类（在“理解 SqlQuery”部分中已介绍）。

第二行代码（使用 `SqlOutParameter`）声明了一个输出参数，用于存储过程调用。此外，还有一个 `SqlInOutParameter` 类型用于 INOUT 参数（即向存储过程提供输入值并返回结果值的参数）。

只有被声明为 `SqlParameter` 和 `SqlInOutParameter` 的参数才会用于提供输入值。这与 `StoredProcedure` 类不同，后者（出于向后兼容性的考虑）允许为声明为 `SqlOutParameter` 的参数提供输入值。

对于输入参数，除了名称和 SQL 类型外，您还可以为数值数据指定精度（scale），或为自定义数据库类型指定类型名称。对于输出参数，您可以提供一个 `RowMapper` 来处理从 REF 游标返回的行数据的映射。另一种选择是声明一个 `SqlReturnType`，它提供了自定义处理返回值的机会。

# 使用 SimpleJdbcCall 调用存储函数

你可以几乎以与调用存储过程相同的方式调用存储函数，只是需要提供函数名而非过程名。在配置中，你使用 `withFunctionName` 方法来表明要调用一个函数，并生成相应的函数调用字符串。使用专门的调用方法（`executeFunction`）来运行该函数，它会将函数的返回值作为指定类型的对象返回，这意味着你无需从结果映射中提取返回值。对于仅有一个输出参数的存储过程，也提供了类似的便捷方法（名为 `executeObject`）。以下示例（针对 MySQL）基于一个名为 `get_actor_name` 的存储函数，该函数返回演员的全名：

```java
CREATE FUNCTION get_actor_name (in_id INTEGER)
RETURNS VARCHAR(200) READS SQL DATA
BEGIN
	DECLARE out_name VARCHAR(200);
	SELECT concat(first_name, ' ', last_name)
		INTO out_name
		FROM t_actor where id = in_id;
	RETURN out_name;
END;
```

要调用此函数，我们再次在初始化方法中创建一个 SimpleJdbcCall，如下例所示：

```java
public class JdbcActorDao implements ActorDao {

	private SimpleJdbcCall funcGetActorName;

	public void setDataSource(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.setResultsMapCaseInsensitive(true);
		this.funcGetActorName = new SimpleJdbcCall(jdbcTemplate)
				.withFunctionName("get_actor_name");
	}

	public String getActorName(Long id) {
		SqlParameterSource in = new MapSqlParameterSource()
				.addValue("in_id", id);
		String name = funcGetActorName.executeFunction(String.class, in);
		return name;
	}

	// ... additional methods
}
```
所使用的 executeFunction 方法返回一个字符串，该字符串包含函数调用的返回值。

# 从 SimpleJdbcCall 返回 ResultSet 或 REF Cursor

调用返回结果集的存储过程或函数有点棘手。有些数据库在JDBC结果处理期间返回结果集，而另一些数据库则要求显式注册特定类型的输出参数。这两种方法都需要额外的处理来遍历结果集并处理返回的行。通过SimpleJdbcCall，你可以使用returningResultSet方法，并声明一个RowMapper实现，用于特定的参数。如果结果集是在结果处理期间返回的，那么没有定义名称，因此返回的结果必须与你声明RowMapper实现的顺序相匹配。指定的名称仍然用于将处理后的结果列表存储在execute语句返回的结果映射中。

下面的示例（针对MySQL）使用一个不带IN参数的存储过程，该过程返回t_actor表中的所有行：
```java
CREATE PROCEDURE read_all_actors()
BEGIN
 SELECT a.id, a.first_name, a.last_name, a.birth_date FROM t_actor a;
END;
```

要调用此过程，您可以声明RowMapper。由于您要映射的类遵循JavaBean规范，您可以使用BeanPropertyRowMapper，该映射器通过在newInstance方法中传入需要映射的类来创建。以下示例展示了如何实现：
```java

public class JdbcActorDao implements ActorDao {

	private SimpleJdbcCall procReadAllActors;

	public void setDataSource(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.setResultsMapCaseInsensitive(true);
		this.procReadAllActors = new SimpleJdbcCall(jdbcTemplate)
				.withProcedureName("read_all_actors")
				.returningResultSet("actors",
				BeanPropertyRowMapper.newInstance(Actor.class));
	}

	public List getActorsList() {
		Map m = procReadAllActors.execute(new HashMap<String, Object>(0));
		return (List) m.get("actors");
	}

	// ... additional methods
}
```
execute 调用传入一个空的 Map，因为该调用不接收任何参数。随后，从结果 Map 中获取演员列表并返回给调用者。