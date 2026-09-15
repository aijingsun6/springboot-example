# Spring中的ORM简介

Spring Framework 支持与 Java 持久化 API（JPA）的集成，并为资源管理、数据访问对象（DAO）实现以及事务策略提供原生 Hibernate 支持。例如，对于 Hibernate，提供了具有多种便捷 IoC 特性的顶级支持，以解决许多典型的 Hibernate 集成问题。您可以通过依赖注入配置所有受支持的 OR（对象关系）映射工具的功能。这些工具可以参与 Spring 的资源和事务管理，并遵循 Spring 的通用事务和 DAO 异常层次结构。推荐的集成方式是使用纯 Hibernate 或 JPA API 编写 DAO。

当您创建数据访问应用程序时，Spring 为所选的 ORM 层添加了显著增强功能。您可以根据需要使用尽可能多的集成支持，并应将此集成工作与内部构建类似基础设施的成本和风险进行比较。无论采用何种技术，您都可以像使用库一样使用大部分 ORM 支持，因为所有内容都被设计为一组可重用的 JavaBean。在 Spring IoC 容器中使用 ORM 有助于配置和部署。因此，本节中的大多数示例都展示了在 Spring 容器内部的配置。

使用 Spring 框架来创建 ORM DAO 的好处包括：

更容易测试。Spring 的 IoC 方式使得切换 Hibernate SessionFactory 实例、JDBC DataSource 实例、事务管理器以及映射对象实现（如果需要）的实现类和配置位置变得非常容易。这反过来又使得可以更容易地单独测试每一段与持久化相关的代码。

通用的数据访问异常。Spring 可以包装来自 ORM 工具的异常，将它们从专有的（可能是受检的）异常转换为通用的运行时 DataAccessException 层次结构。这样，你就可以在适当的层中处理大多数不可恢复的持久化异常，而无需烦人的样板式 catch、throw 和异常声明。当然，你仍然可以按需要捕获和处理异常。请记住，JDBC 异常（包括数据库特定的方言）也会被转换为相同的层次结构，这意味着你可以在一致的编程模型中执行一些 JDBC 操作。

通用的资源管理。Spring 应用上下文可以处理 Hibernate SessionFactory 实例、JPA EntityManagerFactory 实例、JDBC DataSource 实例以及其他相关资源的位置和配置。这使得这些值很容易管理和更改。Spring 提供了高效、简单、安全的持久化资源管理方式。例如，使用 Hibernate 的相关代码通常需要使用同一个 Hibernate Session 以确保效率和正确的事务处理。Spring 通过 Hibernate SessionFactory 暴露当前 Session，可以透明地创建 Session 并将其绑定到当前线程，从而使得 Session 的使用变得非常容易。因此，Spring 解决了典型 Hibernate 使用中的许多长期存在的问题，无论是在本地事务还是 JTA 事务环境中。

集成的声明式事务管理。你可以通过 @Transactional 注解，或者在 XML 配置文件中显式配置事务 AOP 通知，将你的 ORM 代码包装在声明式、面向切面编程（AOP）风格的方法拦截器中。在这两种情况下，事务语义和异常处理（回滚等）都会为你自动处理。正如在“资源与事务管理”一节中所讨论的那样，你还可以切换各种事务管理器，而不会影响你的 ORM 相关代码。例如，你可以在本地事务和 JTA 之间切换，而在这两种场景下，你都可以使用相同的功能（如声明式事务）。此外，JDBC 相关的代码也可以与用于 ORM 的代码在事务上完全集成。这对于不适合使用 ORM 的数据访问（如批处理和 BLOB 流式处理）仍然需要与 ORM 操作共享事务的情况非常有用。

