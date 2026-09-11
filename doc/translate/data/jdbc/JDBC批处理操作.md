# JDBC 批处理操作(JDBC Batch Operations)

大多数JDBC驱动程序在将多个调用批处理到同一个预处理语句时，可以提供更高的性能。通过将更新操作分组为批次，可以减少与数据库之间的往返次数。

# JdbcTemplate基础批量操作

您可以通过实现一个特殊接口 `BatchPreparedStatementSetter` 的两个方法，并将该实现作为 `batchUpdate` 方法调用的第二个参数传入，来完成 JdbcTemplate 的批量处理。您可以使用 `getBatchSize` 方法来提供当前批次的大小。您可以使用 `setValues` 方法来设置预处理语句参数的值。该方法会被调用次数等于 `getBatchSize` 方法中指定的次数。以下示例根据列表中的条目更新 `t_actor` 表，并将整个列表作为批次使用：

```java

public class JdbcActorDao implements ActorDao {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public int[] batchUpdate(final List<Actor> actors) {
		return this.jdbcTemplate.batchUpdate(
				"update t_actor set first_name = ?, last_name = ? where id = ?",
				new BatchPreparedStatementSetter() {
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						Actor actor = actors.get(i);
						ps.setString(1, actor.getFirstName());
						ps.setString(2, actor.getLastName());
						ps.setLong(3, actor.getId().longValue());
					}
					public int getBatchSize() {
						return actors.size();
					}
				});
	}

	// ... additional methods
}
```
如果你正在处理一个更新流或从文件中读取数据，你可能会有一个首选的批量大小，但最后一批可能不会包含该数量的条目。在这种情况下，你可以使用 `InterruptibleBatchPreparedStatementSetter` 接口，它允许你在输入源耗尽时中断批量操作。`isBatchExhausted` 方法可以让你发出批量结束的信号。

# 使用对象列表进行批量操作

JdbcTemplate 和 NamedParameterJdbcTemplate 都提供了另一种执行批量更新的方式。你无需实现专门的批量接口，而是将所有参数值作为列表在调用中提供。框架会遍历这些值，并使用内部的预处理语句设置器。API 的使用方式取决于你是否使用命名参数。如果使用命名参数，你需要提供一个 SqlParameterSource 数组，数组中的每个元素对应批量操作中的一个成员。你可以使用 SqlParameterSourceUtils.createBatch 便利方法来创建这个数组，传入一个包含 bean 风格对象（其 getter 方法与参数对应）的数组、以字符串为键的 Map 实例（包含相应的参数作为值），或者两者的混合体。

以下示例展示了使用命名参数进行批量更新的操作：

```java

public class JdbcActorDao implements ActorDao {

	private NamedParameterTemplate namedParameterJdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	public int[] batchUpdate(List<Actor> actors) {
		return this.namedParameterJdbcTemplate.batchUpdate(
				"update t_actor set first_name = :firstName, last_name = :lastName where id = :id",
				SqlParameterSourceUtils.createBatch(actors));
	}

	// ... additional methods
}
```
对于使用经典占位符“？” 的SQL语句，您需要传入一个包含对象数组的列表，该数组中包含更新值。此对象数组必须为SQL语句中的每个占位符提供一个条目，并且它们的顺序必须与SQL语句中定义的顺序一致。 
以下示例与前面的示例相同，只是它使用了经典的JDBC “？” 占位符：

```java
public class JdbcActorDao implements ActorDao {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public int[] batchUpdate(final List<Actor> actors) {
		List<Object[]> batch = new ArrayList<>();
		for (Actor actor : actors) {
			Object[] values = new Object[] {
					actor.getFirstName(), actor.getLastName(), actor.getId()};
			batch.add(values);
		}
		return this.jdbcTemplate.batchUpdate(
				"update t_actor set first_name = ?, last_name = ? where id = ?",
				batch);
	}

	// ... additional methods
}
```
我们之前描述的所有批量更新方法都会返回一个 int 数组，其中包含每个批量操作条目所影响的行数。该计数由 JDBC 驱动程序报告。如果计数不可用，JDBC 驱动程序将返回值 -2。

在这种场景下，当在底层的 PreparedStatement 上自动设置值时，每个值对应的 JDBC 类型需要从给定的 Java 类型推导得出。虽然这通常运行良好，但仍可能存在问题（例如，Map 中包含的 null 值）。默认情况下，Spring 在这种情况下会调用 ParameterMetaData.getParameterType，这在使用某些 JDBC 驱动时可能会带来较大开销。建议使用较新的驱动版本，并且如果您的应用程序遇到特定的性能问题，可以考虑将 spring.jdbc.getParameterType.ignore 属性设置为 true（作为 JVM 系统属性或通过 SpringProperties 机制）。

从 6.1.2 版本开始，Spring 在 PostgreSQL 和 MS SQL Server 上绕过了默认的 getParameterType 解析。这是一种常见的优化手段，旨在避免为了解析参数类型而向数据库管理系统（DBMS）发起额外的往返请求。这种优化在 PostgreSQL 和 MS SQL Server 上尤其显著，特别是在批量操作中。如果您发现副作用（例如，在未明确指定类型的情况下将字节数组设置为 null），可以通过将 spring.jdbc.getParameterType.ignore=false 作为系统属性显式设置（如上所述），以恢复完整的 getParameterType 解析。

另外，您也可以考虑显式指定相应的 JDBC 类型，方式包括：通过 BatchPreparedStatementSetter（如前所示）、通过为基于 List<Object[]> 的调用提供显式类型数组、通过在自定义的 MapSqlParameterSource 实例上调用 registerSqlType 方法、通过使用 BeanPropertySqlParameterSource（即使值为 null，也能从 Java 声明的属性类型推导出 SQL 类型），或者通过提供单独的 SqlParameterValue 实例来替代普通的 null 值。

# 多批次批量操作

前面的批量更新示例处理的是那些规模非常大的批次，以至于你希望将它们拆分为几个较小的批次。你可以通过之前提到的方法，多次调用 `batchUpdate` 方法来实现这一点，但现在有了一个更方便的方法。该方法除了接收 SQL 语句外，还接收一个包含参数的对象集合、每个批次要执行的更新次数，以及一个 `ParameterizedPreparedStatementSetter` 用于设置预处理语句的参数值。框架会遍历提供的值，并将更新调用拆分为指定大小的批次。

以下示例展示了一个使用批量大小为 100 的批量更新操作：
```java
public class JdbcActorDao implements ActorDao {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public int[][] batchUpdate(final Collection<Actor> actors) {
		int[][] updateCounts = jdbcTemplate.batchUpdate(
				"update t_actor set first_name = ?, last_name = ? where id = ?",
				actors,
				100,
				(PreparedStatement ps, Actor actor) -> {
					ps.setString(1, actor.getFirstName());
					ps.setString(2, actor.getLastName());
					ps.setLong(3, actor.getId().longValue());
				});
		return updateCounts;
	}

	// ... additional methods
}
```
该调用的批量更新方法返回一个整数数组的数组，其中每个批次对应一个数组条目，包含该批次中每个更新操作所影响的行数。顶层数组的长度表示执行的批次数量，第二层数组的长度表示该批次中的更新数量。每个批次中的更新数量应为所有批次提供的批量大小（最后一个批次可能较少），具体取决于提供的更新对象总数。每个更新语句的更新计数由JDBC驱动程序报告。如果计数不可用，JDBC驱动程序将返回值-2。
