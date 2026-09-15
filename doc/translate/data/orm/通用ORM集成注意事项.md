# 通用ORM集成注意事项

本节重点介绍了适用于所有ORM技术的考虑事项。Hibernate部分提供了更多细节，并在具体上下文中展示了这些特性和配置。

Spring的ORM集成主要目标是实现清晰的应用层划分（无论使用何种数据访问和事务技术），并实现应用对象的松耦合——业务服务不再依赖于数据访问或事务策略，不再有硬编码的资源查找，不再有难以替换的单例模式，也不再需要自定义的服务注册机制。其目标是采用一种简单而一致的方式来装配应用对象，使它们尽可能可重用且不依赖于容器。所有单独的数据访问特性都可以独立使用，但它们与Spring的应用上下文概念结合得非常紧密，提供了基于XML的配置方式，并能对普通的JavaBean实例进行交叉引用，而这些JavaBean实例无需具备Spring的感知能力。在典型的Spring应用中，许多重要对象都是JavaBean：数据访问模板、数据访问对象、事务管理器、使用数据访问对象和事务管理器的业务服务、Web视图解析器、使用业务服务的Web控制器等等。


# 资源与事务管理
典型的业务应用中充斥着重复的资源管理代码。许多项目试图自行设计解决方案，有时为了编程方便而牺牲了错误的正确处理。Spring 提倡通过简单的方法来正确处理资源，具体而言，对于 JDBC 采用模板化方式实现 IoC，而对于 ORM 技术则采用 AOP 拦截器。

该基础设施提供了正确的资源处理机制，并将特定 API 异常转换为不受检查的基础设施异常层次结构。Spring 引入了一个 DAO 异常层次结构，适用于任何数据访问策略。对于直接的 JDBC，前面章节中提到的 JdbcTemplate 类提供了连接处理，并将 SQLException 转换为 DataAccessException 层次结构，包括将数据库特定的 SQL 错误码转换为有意义的异常类。对于 ORM 技术，请参阅下一节，了解如何获得相同的异常转换优势。

在事务管理方面，JdbcTemplate 类集成了 Spring 的事务支持，并通过相应的 Spring 事务管理器支持 JTA 和 JDBC 事务。对于支持的 ORM 技术，Spring 通过 Hibernate 和 JPA 事务管理器以及 JTA 支持来提供 Hibernate 和 JPA 的支持。有关事务支持的详细信息，请参阅事务管理章节。

# 异常转换
当您在DAO中使用Hibernate或JPA时，必须决定如何处理持久化技术的原生异常类。DAO会抛出HibernateException或PersistenceException的子类，具体取决于所使用的技术。这些异常都是运行时异常，无需声明或捕获。此外，您可能还需要处理IllegalArgumentException和IllegalStateException。这意味着调用者只能将异常视为通常致命的错误，除非他们希望依赖持久化技术自身的异常结构。如果不将调用者绑定到实现策略上，就无法捕获特定的原因（例如乐观锁定失败）。对于那些高度依赖ORM或不需要特殊异常处理（或两者兼具）的应用程序来说，这种权衡可能是可以接受的。然而，Spring允许通过@Repository注解透明地应用异常转换。以下示例（一个用于Java配置，一个用于XML配置）展示了如何实现这一点：

```java

@Repository
public class ProductDaoImpl implements ProductDao {

	// class body here...

}
```

```xml

<beans>

	<!-- Exception translation bean post processor -->
	<bean class="org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor"/>

	<bean id="myProductDao" class="product.ProductDaoImpl"/>

</beans>
```
后处理器会自动查找所有异常转换器（实现了 `PersistenceExceptionTranslator` 接口的实现类），并为所有标记了 `@Repository` 注解的 bean 提供建议，以便发现的转换器能够拦截并应用适当的转换到抛出的异常上。 
总之，你可以基于纯持久化技术的 API 和注解来实现 DAO，同时仍然能够受益于 Spring 管理的事务、依赖注入以及透明的异常转换（如果需要的话），从而将异常转换为 Spring 自定义的异常层级结构。

