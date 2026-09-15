# Hibernate
我们首先介绍在Spring环境中使用Hibernate 5，并通过它来展示Spring在集成ORM映射器方面所采用的方法。本节详细讨论了许多问题，并展示了DAO实现和事务边界划分的不同变体。其中大多数模式可以直接应用于所有其他支持的ORM工具。本章后续部分则介绍了其他ORM技术，并提供了简要示例。


截至Spring Framework 6.0，Spring要求使用Hibernate ORM 5.5+版本，以支持Spring的HibernateJpaVendorAdapter以及原生的Hibernate SessionFactory配置。我们推荐使用Hibernate ORM 5.6，因为它是该Hibernate版本系列中最后一个功能分支。

Hibernate ORM 6.x主要作为JPA提供程序（HibernateJpaVendorAdapter）被支持。出于迁移目的，使用orm.hibernate5包进行纯SessionFactory配置是被允许的。对于新的开发项目，我们建议使用JPA风格的配置配合Hibernate ORM 6.x。


# SessionFactory 

为避免将应用程序对象与硬编码的资源查找绑定，您可以在Spring容器中定义资源（如JDBC数据源或Hibernate SessionFactory）为Bean。需要访问资源的应用程序对象通过Bean引用获取这些预定义实例的引用，如下一节的DAO定义所示。 
以下XML应用上下文定义的摘录展示了如何在之上配置JDBC数据源和Hibernate SessionFactory：

```xml
<beans>

	<bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
		<property name="driverClassName" value="org.hsqldb.jdbcDriver"/>
		<property name="url" value="jdbc:hsqldb:hsql://localhost:9001"/>
		<property name="username" value="sa"/>
		<property name="password" value=""/>
	</bean>

	<bean id="mySessionFactory" class="org.springframework.orm.hibernate5.LocalSessionFactoryBean">
		<property name="dataSource" ref="myDataSource"/>
		<property name="mappingResources">
			<list>
				<value>product.hbm.xml</value>
			</list>
		</property>
		<property name="hibernateProperties">
			<value>
				hibernate.dialect=org.hibernate.dialect.HSQLDialect
			</value>
		</property>
	</bean>

</beans>

```
从本地的 Jakarta Commons DBCP BasicDataSource 切换到位于 JNDI 中的数据源（通常由应用服务器管理）仅需进行配置，如下例所示：


```xml
<beans>
	<jee:jndi-lookup id="myDataSource" jndi-name="java:comp/env/jdbc/myds"/>
</beans>
```

您还可以通过 Spring 的 JndiObjectFactoryBean / <jee:jndi-lookup> 来访问位于 JNDI 中的 SessionFactory 并对其进行引用。然而，这通常仅在 EJB 上下文之外不常见。

Spring 还提供了一个 LocalSessionFactoryBuilder 变体，可以无缝集成 @Bean 风格的配置和编程式设置（不涉及 FactoryBean）。

LocalSessionFactoryBean 和 LocalSessionFactoryBuilder 都支持后台引导，Hibernate 初始化会在给定的引导执行器（例如 SimpleAsyncTaskExecutor）上与应用程序引导线程并行运行。在 LocalSessionFactoryBean 中，可通过 bootstrapExecutor 属性实现此功能。在编程式的 LocalSessionFactoryBuilder 中，有一个重载的 buildSessionFactory 方法，该方法接受一个引导执行器参数。

这种原生的 Hibernate 设置还可以暴露一个 JPA EntityManagerFactory，用于在原生 Hibernate 访问之外进行标准的 JPA 交互。详见 Native Hibernate Setup for JPA。

# 基于纯 Hibernate API 实现 DAO
基于纯 Hibernate API 实现 DAO 
Hibernate 有一个称为上下文会话（contextual sessions）的特性，其中 Hibernate 本身会为每个事务管理一个当前的 Session。这大致相当于 Spring 为每个事务同步一个 Hibernate Session。基于纯 Hibernate API 的相应 DAO 实现示例如下：

```java


public class ProductDaoImpl implements ProductDao {

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Collection loadProductsByCategory(String category) {
		return this.sessionFactory.getCurrentSession()
				.createQuery("from test.Product product where product.category=?")
				.setParameter(0, category)
				.list();
	}
}
```

这种风格与Hibernate参考文档和示例中的风格类似，只是将SessionFactory保存在实例变量中。我们强烈推荐这种基于实例的设置方式，而不是Hibernate CaveatEmptor示例应用中那种老式的静态HibernateUtil类。（一般来说，除非绝对必要，否则不要将任何资源保存在静态变量中。）

前面的DAO示例遵循了依赖注入模式。它非常适合集成到Spring IoC容器中，就像使用Spring的HibernateTemplate编写代码一样。你也可以在纯Java环境中（例如在单元测试中）设置这样的DAO。为此，可以实例化它，并调用setSessionFactory(..)方法传入所需的工厂引用。作为Spring的bean定义，该DAO的定义可能如下所示：

```xml


<beans>

	<bean id="myProductDao" class="product.ProductDaoImpl">
		<property name="sessionFactory" ref="mySessionFactory"/>
	</bean>

</beans>
```


这种DAO风格的主要优点在于它仅依赖于Hibernate API，无需导入任何Spring类。从非侵入性的角度来看，这颇具吸引力，对Hibernate开发者而言可能感觉更为自然。

然而，这种DAO抛出的是普通的HibernateException（它属于非检查型异常，因此无需声明或捕获），这意味着调用者只能将异常视为通常的致命错误——除非他们愿意依赖Hibernate自身的异常体系。若要捕获特定原因（例如乐观锁失败），除非将调用者与实现策略绑定，否则无法做到。对于那些深度依赖Hibernate、无需特殊异常处理或两者兼有的应用程序来说，这种权衡或许可以接受。

幸运的是，Spring的LocalSessionFactoryBean支持Hibernate的SessionFactory.getCurrentSession()方法，适用于任何Spring事务策略，并能返回当前由Spring管理的事务性Session，即使在使用HibernateTransactionManager时也是如此。该方法的标准行为仍然是返回与当前JTA事务（如果有的话）关联的Session。无论你使用的是Spring的JtaTransactionManager、EJB容器管理事务（CMT）还是JTA，这种行为都适用。

总之，你可以基于纯Hibernate API实现DAO，同时仍能参与Spring管理的事务。

# 声明式事务界定
我们建议您使用 Spring 的声明式事务支持，它允许您在 Java 代码中用 AOP 事务拦截器替换显式的事务界定 API 调用。您可以通过 Java 注解或 XML 在 Spring 容器中配置该事务拦截器。这种声明式事务能力使您能够将业务服务与重复的事务界定代码分离，从而专注于添加业务逻辑，这才是应用程序真正的价值所在。

在继续之前，如果您尚未阅读过《声明式事务管理》一节，我们强烈建议您先阅读该内容。

您可以在服务层使用 @Transactional 注解，指示 Spring 容器查找这些注解并为这些被注解的方法提供事务语义。以下示例展示了如何实现这一点：

```java

public class ProductServiceImpl implements ProductService {

	private ProductDao productDao;

	public void setProductDao(ProductDao productDao) {
		this.productDao = productDao;
	}

	@Transactional
	public void increasePriceOfAllProductsInCategory(final String category) {
		List productsToChange = this.productDao.loadProductsByCategory(category);
		// ...
	}

	@Transactional(readOnly = true)
	public List<Product> findAllProducts() {
		return this.productDao.findAllProducts();
	}
}
```

在容器中，您需要设置一个 PlatformTransactionManager 的实现（作为 bean）以及一个 <tx:annotation-driven/> 条目，以便在运行时启用 @Transactional 的处理。以下示例展示了如何实现这一点：

```java

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:aop="http://www.springframework.org/schema/aop"
	xmlns:tx="http://www.springframework.org/schema/tx"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/tx
		https://www.springframework.org/schema/tx/spring-tx.xsd
		http://www.springframework.org/schema/aop
		https://www.springframework.org/schema/aop/spring-aop.xsd">

	<!-- SessionFactory, DataSource, etc. omitted -->

	<bean id="transactionManager"
			class="org.springframework.orm.hibernate5.HibernateTransactionManager">
		<property name="sessionFactory" ref="sessionFactory"/>
	</bean>

	<tx:annotation-driven/>

	<bean id="myProductService" class="product.SimpleProductService">
		<property name="productDao" ref="myProductDao"/>
	</bean>

</beans>
```

# 程序化事务界定

你可以在应用程序的更高层级上划分事务，这些事务建立在跨越任意数量操作的下层数据访问服务之上。同时，对周边业务服务的实现也不存在限制。它只需要一个 Spring PlatformTransactionManager。同样，后者可以来自任何地方，但最好通过 setTransactionManager(..) 方法以 bean 的形式进行引用。此外，productDAO 也应通过 setProductDao(..) 方法进行设置。以下代码片段展示了 Spring 应用上下文中事务管理器和业务服务的定义，以及一个业务方法实现的示例：

```xml

<beans>

	<bean id="myTxManager" class="org.springframework.orm.hibernate5.HibernateTransactionManager">
		<property name="sessionFactory" ref="mySessionFactory"/>
	</bean>

	<bean id="myProductService" class="product.ProductServiceImpl">
		<property name="transactionManager" ref="myTxManager"/>
		<property name="productDao" ref="myProductDao"/>
	</bean>

</beans>
```

```java

public class ProductServiceImpl implements ProductService {

	private TransactionTemplate transactionTemplate;
	private ProductDao productDao;

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public void setProductDao(ProductDao productDao) {
		this.productDao = productDao;
	}

	public void increasePriceOfAllProductsInCategory(final String category) {
		this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			public void doInTransactionWithoutResult(TransactionStatus status) {
				List productsToChange = this.productDao.loadProductsByCategory(category);
				// do the price increase...
			}
		});
	}
}
```


Spring 的 TransactionInterceptor 允许在回调代码中抛出任何已检查的应用程序异常，而 TransactionTemplate 仅限于在回调中抛出未检查异常。当发生未检查的应用程序异常，或者应用程序将事务标记为仅回滚（通过设置 TransactionStatus）时，TransactionTemplate 会触发回滚。默认情况下，TransactionInterceptor 的行为与此相同，但允许为每个方法配置回滚策略。


# 事务管理策略

`TransactionTemplate` 和 `TransactionInterceptor` 都将实际的事务处理委托给一个 `PlatformTransactionManager` 实例（在底层，它可以是 `HibernateTransactionManager`（用于单个 Hibernate SessionFactory，通过使用 `ThreadLocal` Session））或者 `JtaTransactionManager`（用于将事务委托给容器中的 JTA 子系统）——对于 Hibernate 应用程序而言。你甚至可以使用自定义的 `PlatformTransactionManager` 实现。从原生 Hibernate 事务管理切换到 JTA（例如，当你的应用程序部署需要分布式事务时），只需要进行配置即可。你可以用 Spring 的 JTA 事务实现替换 Hibernate 的事务管理器。事务界定和数据访问代码无需任何改动，因为它们使用的是通用的事务管理 API。

对于跨多个 Hibernate SessionFactory 的分布式事务，你可以将 `JtaTransactionManager` 作为事务策略，与多个 `LocalSessionFactoryBean` 的定义结合起来使用。这样，每个 DAO 都会获得一个特定的 `SessionFactory` 引用，并将其注入到相应的 bean 属性中。如果所有底层的 JDBC 数据源都是事务性容器数据源，那么只要业务服务使用 `JtaTransactionManager` 作为事务策略，它就可以跨任意数量的 DAO 和 SessionFactory 来界定事务，而无需特别关注。

`HibernateTransactionManager` 和 `JtaTransactionManager` 都允许在 Hibernate 中正确处理 JVM 级别的缓存，而无需依赖容器特定的事务管理器查找机制，也无需 JCA 连接器（如果你不使用 EJB 来发起事务）。

`HibernateTransactionManager` 可以将 Hibernate 的 JDBC Connection 导出给特定的 DataSource，以供纯 JDBC 访问代码使用。这种能力允许你在不使用 JTA 的情况下，对 Hibernate 和 JDBC 混合的数据访问进行高级事务界定，前提是只访问一个数据库。如果你通过 `LocalSessionFactoryBean` 的 `dataSource` 属性，将传入的 `SessionFactory` 与 DataSource 关联起来，`HibernateTransactionManager` 会自动将 Hibernate 事务暴露为 JDBC 事务。或者，你也可以通过 `HibernateTransactionManager` 的 `dataSource` 属性，显式地指定需要暴露事务的 DataSource。

为了实现 JTA 风格的延迟获取实际资源连接，Spring 提供了相应的 DataSource 代理类来代理目标连接池：参见 `LazyConnectionDataSourceProxy`。这对于 Hibernate 的只读事务特别有用，因为只读事务通常可以从本地缓存中处理，而无需访问数据库。

# 比较容器管理资源与本地定义资源


您可以在容器管理的JNDI SessionFactory和本地定义的SessionFactory之间切换，而无需更改应用程序代码中的任何一行。是否将资源定义保留在容器中，还是保留在应用程序的本地，主要取决于您采用的事务策略。与Spring定义的本地SessionFactory相比，手动注册的JNDI SessionFactory并没有任何优势。通过Hibernate的JCA连接器部署SessionFactory，可以参与Jakarta EE服务器的管理基础设施，从而获得附加价值，但除此之外并没有实际价值。

Spring的事务支持并不依赖于容器。当配置为除JTA以外的任何策略时，事务支持同样可以在独立环境或测试环境中运行。尤其是在典型的单数据库事务场景中，Spring的单资源本地事务支持是JTA的一种轻量级且功能强大的替代方案。当您使用本地EJB无状态会话Bean来驱动事务时，即使只访问一个数据库，并且只使用无状态会话Bean通过容器管理的事务来提供声明式事务，您也必须依赖EJB容器和JTA。直接以编程方式使用JTA同样需要Jakarta EE环境。

Spring驱动的事务可以像使用本地JDBC DataSource一样，与本地定义的Hibernate SessionFactory配合良好，前提是它们只访问一个数据库。因此，只有在分布式事务需求的情况下，您才需要使用Spring的JTA事务策略。JCA连接器需要容器特定的部署步骤，而且（显然）首先需要JCA支持。这种配置比部署一个带有本地资源定义和Spring驱动事务的简单Web应用程序需要更多的工作。

综合考虑，如果您不使用EJB，就坚持使用本地SessionFactory配置以及Spring的HibernateTransactionManager或JtaTransactionManager。这样您可以获得所有好处，包括事务性的JVM级缓存和分布式事务，而无需忍受容器部署带来的不便。通过JCA连接器注册Hibernate SessionFactory的JNDI，只有在与EJB配合使用时，才能体现其附加价值。


# Hibernate 中的虚假应用服务器警告(Spurious Application Server Warnings with Hibernate)

在某些具有非常严格的XADataSource实现（目前包括某些WebLogic Server和WebSphere版本）的JTA环境中，如果Hibernate的配置未考虑该环境中的JTA事务管理器，应用程序服务器日志中可能会出现虚假的警告或异常。这些警告或异常表明正在访问的连接已不再有效，或者JDBC访问已不再有效，这可能是因为事务已不再处于活动状态。例如，以下是WebLogic中的实际异常示例：

```java

java.sql.SQLException: The transaction is no longer active - status: 'Committed'. No
further JDBC access is allowed within this transaction.
```
另一个常见问题是 JTA 事务之后出现连接泄漏，Hibernate 会话（以及潜在的底层 JDBC 连接）没有正确关闭。 
您可以通过让 Hibernate 了解 JTA 事务管理器（Spring 也会同步到该管理器）来解决此类问题。您有两种方式可以实现这一点： 
将 Spring 的 JtaTransactionManager bean 传递给 Hibernate 的配置。最简单的方式是将 bean 引用注入到 LocalSessionFactoryBean 的 jtaTransactionManager 属性中（参见 Hibernate 事务配置）。Spring 会将相应的 JTA 策略提供给 Hibernate。 
您也可以显式地配置 Hibernate 的 JTA 相关属性，具体来说，就是 LocalSessionFactoryBean 的 hibernateProperties 中的 "hibernate.transaction.coordinator_class"、"hibernate.connection.handling_mode"，以及可能的 "hibernate.transaction.jta.platform"（有关这些属性的详细信息，请参阅 Hibernate 的手册）。 

本节其余部分描述了在 Hibernate 了解 JTA PlatformTransactionManager 的情况下，以及不了解该管理器的情况下，所发生的事件序列。 

当 Hibernate 没有配置对 JTA 事务管理器的任何了解时，JTA 事务提交时会发生以下事件： 
JTA 事务提交。 
Spring 的 JtaTransactionManager 会同步到 JTA 事务，因此 JTA 事务管理器会通过 afterCompletion 回调函数回调它。 
这种同步会触发 Spring 通过 Hibernate 的 afterTransactionCompletion 回调函数（用于清除 Hibernate 缓存）回调 Hibernate，然后显式地调用 Hibernate 会话的 close() 方法，这会导致 Hibernate 尝试关闭 JDBC 连接。 
在某些环境中，这个 Connection.close() 调用会触发警告或错误，因为应用服务器不再认为该连接可用，因为事务已经提交。 

当 Hibernate 配置了对 JTA 事务管理器的了解时，JTA 事务提交时会发生以下事件： 
JTA 事务准备提交。 
Spring 的 JtaTransactionManager 会同步到 JTA 事务，因此 JTA 事务管理器会通过 beforeCompletion 回调函数回调它。 
Spring 知道 Hibernate 本身已经同步到 JTA 事务，因此其行为与前一种情况不同。具体来说，它会与 Hibernate 的事务资源管理保持一致。 
JTA 事务提交。 
Hibernate 会同步到 JTA 事务，因此 JTA 事务管理器会通过 afterCompletion 回调函数回调它，Hibernate 可以正确地清除其缓存。


# 参考文档
- [https://hibernate.org](https://hibernate.org/)