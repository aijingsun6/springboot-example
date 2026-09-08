# DAO Support

Spring 中的数据访问对象（DAO）支持旨在以一致的方式简化与数据访问技术（如 JDBC、Hibernate 或 JPA）的协作。这使得您可以轻松地在上述持久化技术之间切换，同时也让您在编码时无需担心捕获每种技术特有的异常。

# 一致的异常层次结构

Spring 提供了一种便捷的机制，可以将特定于技术的异常（如 SQLException）转换为自身的异常类层次结构，该结构的根异常是 DataAccessException。这些异常会包装原始异常，从而确保你永远不会丢失任何关于可能出错原因的信息。

除了 JDBC 异常之外，Spring 还可以包装 JPA 和 Hibernate 特定的异常，将它们转换为一组聚焦的运行时异常。这样，你只需在适当的层中处理大多数不可恢复的持久化异常，而无需在 DAO 中编写烦人的样板式 catch-throw 代码块和异常声明。（当然，你仍然可以在需要的地方捕获和处理异常。）如上所述，JDBC 异常（包括数据库特定的方言）也会被转换为相同的异常层次结构，这意味着你可以在一致的编程模型中执行一些 JDBC 操作。

上述讨论同样适用于 Spring 支持各种 ORM 框架时所提供的各种模板类。如果你使用的是基于拦截器的类，应用程序必须自行处理 HibernateException 和 PersistenceException，最好通过委托给 SessionFactoryUtils 的 convertHibernateAccessException(..) 或 convertJpaAccessException(..) 方法来实现。这些方法会将异常转换为与 org.springframework.dao 异常层次结构中异常兼容的异常。由于 PersistenceException 是非检查型异常，它们也可能被抛出（不过这样一来，就牺牲了异常方面的通用 DAO 抽象）。

下图展示了 Spring 提供的异常层次结构。（请注意，图中详细展示的类层次结构仅显示了整个 DataAccessException 层次结构的一个子集。）

# 用于配置DAO或Repository类的注解

确保你的数据访问对象（DAO）或存储库能够进行异常转换的最佳方式是使用 @Repository 注解。该注解还使组件扫描支持能够找到并配置你的 DAO 和存储库，而无需为其提供 XML 配置条目。以下示例展示了如何使用 @Repository 注解：

```java
@Repository
public class SomeMovieFinder implements MovieFinder {
	// ...
}
```

任何 DAO 或存储库实现都需要访问持久化资源，具体取决于所使用的持久化技术。例如，基于 JDBC 的存储库需要访问 JDBC DataSource，而基于 JPA 的存储库则需要访问 EntityManager。实现这一点最简单的方法是使用 @Autowired、@Inject、@Resource 或 @PersistenceContext 注解之一来注入该资源依赖。以下示例适用于 JPA 存储库：


```java
@Repository
public class JpaMovieFinder implements MovieFinder {

	@PersistenceContext
	private EntityManager entityManager;

	// ...
}
```
如果你使用经典的 Hibernate API，可以注入 SessionFactory，如下例所示：

```java

@Repository
public class HibernateMovieFinder implements MovieFinder {

	private SessionFactory sessionFactory;

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	// ...
}
```

我们在此展示的最后一个示例是典型的JDBC支持。你可以将DataSource注入到初始化方法或构造函数中，在其中使用该DataSource创建JdbcTemplate和其他数据访问支持类（如SimpleJdbcCall等）。以下示例自动装配了一个DataSource
```java
@Repository
public class JdbcMovieFinder implements MovieFinder {

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public void init(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	// ...
}
```