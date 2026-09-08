# 选择 JDBC 数据库访问的方法

您可以选择几种方法来构建 JDBC 数据库访问的基础。除了三种不同风格的 JdbcTemplate 外，SimpleJdbcInsert 和 SimpleJdbcCall 方法可以优化数据库元数据，而 RDBMS 对象风格则能实现更面向对象的处理方式。一旦您开始使用其中一种方法，仍然可以混合搭配，以包含其他方法中的特性。

JdbcTemplate 是经典且最流行的 Spring JDBC 方法。这种“最低级别”的方法以及所有其他方法，其底层都使用了 JdbcTemplate。

NamedParameterJdbcTemplate 封装了 JdbcTemplate，以提供命名参数，而不是传统的 JDBC 占位符“？”。当 SQL 语句包含多个参数时，这种方法能提供更好的文档说明和更简便的使用体验。

SimpleJdbcInsert 和 SimpleJdbcCall 通过优化数据库元数据，来减少必要的配置量。这种方法简化了编码，您只需提供表名或存储过程名，以及一个与列名匹配的参数映射。这种方法仅在数据库提供充分元数据的情况下才有效。如果数据库未提供这些元数据，则必须显式配置参数。

RDBMS 对象（包括 MappingSqlQuery、SqlUpdate 和 StoredProcedure）要求您在数据访问层的初始化阶段创建可重用且线程安全的对象。这种方法允许您定义查询字符串、声明参数并编译查询。一旦完成这些操作，就可以多次调用 execute（…）、update（…） 和 findObject（…） 方法，并传入不同的参数值。