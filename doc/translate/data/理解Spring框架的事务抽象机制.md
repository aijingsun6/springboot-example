#  理解 Spring 框架的事务抽象机制

Understanding the Spring Framework Transaction Abstraction

Spring 事务抽象的关键在于事务策略的概念。事务策略由 `TransactionManager` 定义，具体来说，命令式事务管理使用的是 `org.springframework.transaction.PlatformTransactionManager` 接口，而响应式事务管理使用的是 `org.springframework.transaction.ReactiveTransactionManager` 接口。以下代码清单展示了 `PlatformTransactionManager` API 的定义：

```java

public interface PlatformTransactionManager extends TransactionManager {

	TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;

	void commit(TransactionStatus status) throws TransactionException;

	void rollback(TransactionStatus status) throws TransactionException;
}
```

这主要是一个服务提供者接口（SPI），尽管你也可以从应用程序代码中以编程方式使用它。由于 `PlatformTransactionManager` 是一个接口，因此可以根据需要轻松地进行模拟或存根处理。它并不绑定于特定的查找策略（例如 JNDI）。`PlatformTransactionManager` 的实现方式与 Spring 框架 IoC 容器中的其他对象（或 bean）一样进行定义。仅凭这一优势，Spring 框架的事务抽象就具有了很高的价值，即使在使用 JTA 的情况下也是如此。与直接使用 JTA 相比，你可以更轻松地测试事务代码。

同样，为了符合 Spring 的理念，`PlatformTransactionManager` 接口的任何方法都可能抛出的 `TransactionException` 是非检查型异常（即它扩展了 `java.lang.RuntimeException` 类）。事务基础设施的故障几乎总是致命的。在极少数情况下，如果应用程序代码确实可以从事务失败中恢复，应用程序开发者仍然可以选择捕获并处理 `TransactionException`。关键点在于开发者并不被强制要求这样做。

`getTransaction(..)` 方法根据 `TransactionDefinition` 参数返回一个 `TransactionStatus` 对象。返回的 `TransactionStatus` 可能代表一个新事务，也可能代表一个现有事务（如果当前调用栈中存在匹配的事务）。在后一种情况下，其含义是：与 Jakarta EE 的事务上下文一样，`TransactionStatus` 与执行线程相关联。

Spring 还为使用反应式类型或 Kotlin 协程的反应式应用程序提供了事务管理抽象。以下代码清单展示了 `org.springframework.transaction.ReactiveTransactionManager` 定义的事务策略：

```java

public interface ReactiveTransactionManager extends TransactionManager {

	Mono<ReactiveTransaction> getReactiveTransaction(TransactionDefinition definition) throws TransactionException;

	Mono<Void> commit(ReactiveTransaction status) throws TransactionException;

	Mono<Void> rollback(ReactiveTransaction status) throws TransactionException;
}
```

反应式事务管理器主要是一个服务提供者接口（SPI），尽管你也可以从应用程序代码中以编程方式使用它。由于 ReactiveTransactionManager 是一个接口，因此可以根据需要轻松地进行模拟或存根处理。

`TransactionDefinition` 接口规定了以下内容：

传播行为（Propagation）：通常，事务作用域内的所有代码都在该事务中运行。但是，如果事务方法在已经存在事务上下文的情况下运行，你可以指定其行为。例如，代码可以继续在现有事务中运行（这是常见情况），或者可以挂起现有事务并创建一个新的事务。Spring 提供了所有在 EJB CMT 中熟悉的事务传播选项。要了解 Spring 中事务传播的语义，请参见事务传播。

隔离级别（Isolation）：该事务与其他事务的工作隔离程度。例如，该事务是否可以看到其他事务尚未提交的写操作？

超时（Timeout）：该事务在超时前运行多长时间，之后将由底层事务基础设施自动回滚。

只读状态（Read-only status）：当你的代码只读取数据而不修改数据时，可以使用只读事务。在某些情况下，只读事务可以是一种有用的优化，例如在使用 Hibernate 时。

这些设置反映了标准的事务概念。如有必要，请参考讨论事务隔离级别和其他核心事务概念的资源。理解这些概念对于使用 Spring 框架或任何事务管理解决方案至关重要。

`TransactionStatus` 接口为事务代码提供了一种简单的方式来控制事务的执行并查询事务状态。这些概念应该很熟悉，因为它们是所有事务 API 的共同特性。以下代码清单展示了 `TransactionStatus` 接口：
```java
public interface TransactionStatus extends TransactionExecution, SavepointManager, Flushable {

	@Override
	boolean isNewTransaction();

	boolean hasSavepoint();

	@Override
	void setRollbackOnly();

	@Override
	boolean isRollbackOnly();

	void flush();

	@Override
	boolean isCompleted();
}
```
无论您选择在 Spring 中使用声明式还是编程式事务管理，定义正确的 TransactionManager 实现都是绝对必要的。通常，您可以通过依赖注入来定义此实现。 
TransactionManager 的实现通常需要了解其运行的环境：JDBC、JTA、Hibernate 等等。以下示例展示了如何定义一个本地的 PlatformTransactionManager 实现（在本例中，使用的是纯 JDBC）。 
您可以通过创建一个类似以下的 bean 来定义一个 JDBC DataSource：
```java
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
	<property name="driverClassName" value="${jdbc.driverClassName}" />
	<property name="url" value="${jdbc.url}" />
	<property name="username" value="${jdbc.username}" />
	<property name="password" value="${jdbc.password}" />
</bean>
```
相关的 PlatformTransactionManager bean 定义随后会引用 DataSource 的定义。它应该类似于以下示例：
```java

<bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
	<property name="dataSource" ref="dataSource"/>
</bean>
```
如果你在Jakarta EE容器中使用JTA，那么你需要通过JNDI获取容器DataSource，并结合Spring的JtaTransactionManager使用。以下示例展示了JTA和JNDI查找版本的实现方式：

```java

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:jee="http://www.springframework.org/schema/jee"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/jee
		https://www.springframework.org/schema/jee/spring-jee.xsd">

	<jee:jndi-lookup id="dataSource" jndi-name="jdbc/jpetstore"/>

	<bean id="txManager" class="org.springframework.transaction.jta.JtaTransactionManager" />

	<!-- other <bean/> definitions here -->

</beans>
```

JtaTransactionManager 不需要了解 DataSource（或任何其他特定资源），因为它使用的是容器的事务管理基础设施。

如果你使用 JTA，无论你使用的是 JDBC、Hibernate JPA 还是任何其他受支持的数据访问技术，你的事务管理器定义都应该保持一致。这是因为 JTA 事务是全局事务，可以包含任何事务性资源。

在所有Spring事务配置中，应用程序代码无需更改。即使这种更改意味着从事务管理从本地切换到全局，或从全局切换到本地，您也可以仅通过修改配置来改变事务的管理方式。

# Hibernate事务设置

您也可以像以下示例中那样轻松使用 Hibernate 本地事务。在这种情况下，您需要定义一个 Hibernate LocalSessionFactoryBean，您的应用程序代码可以使用它来获取 Hibernate Session 实例。

DataSource bean 的定义与前面展示的本地 JDBC 示例类似，因此以下示例中未再展示。

如果 DataSource（被任何非 JTA 事务管理器使用）是通过 JNDI 查找并由 Jakarta EE 容器管理的，那么它应该是非事务性的，因为事务由 Spring 框架（而不是 Jakarta EE 容器）管理。

在这种情况下，txManager bean 的类型是 HibernateTransactionManager。与 DataSourceTransactionManager 需要引用 DataSource 一样，HibernateTransactionManager 也需要引用 SessionFactory。以下示例声明了 sessionFactory 和 txManager bean：

```java

<bean id="sessionFactory" class="org.springframework.orm.hibernate5.LocalSessionFactoryBean">
	<property name="dataSource" ref="dataSource"/>
	<property name="mappingResources">
		<list>
			<value>org/springframework/samples/petclinic/hibernate/petclinic.hbm.xml</value>
		</list>
	</property>
	<property name="hibernateProperties">
		<value>
			hibernate.dialect=${hibernate.dialect}
		</value>
	</property>
</bean>

<bean id="txManager" class="org.springframework.orm.hibernate5.HibernateTransactionManager">
	<property name="sessionFactory" ref="sessionFactory"/>
</bean>
```
如果你使用 Hibernate 和 Jakarta EE 容器管理的 JTA 事务，你应该使用与之前 JDBC 的 JTA 示例中相同的 `JtaTransactionManager`，如下例所示。此外，建议通过 Hibernate 的事务协调器（transaction coordinator）以及可能的连接释放模式配置，让 Hibernate 了解 JTA 的存在：

```java
<bean id="sessionFactory" class="org.springframework.orm.hibernate5.LocalSessionFactoryBean">
	<property name="dataSource" ref="dataSource"/>
	<property name="mappingResources">
		<list>
			<value>org/springframework/samples/petclinic/hibernate/petclinic.hbm.xml</value>
		</list>
	</property>
	<property name="hibernateProperties">
		<value>
			hibernate.dialect=${hibernate.dialect}
			hibernate.transaction.coordinator_class=jta
			hibernate.connection.handling_mode=DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT
		</value>
	</property>
</bean>

<bean id="txManager" class="org.springframework.transaction.jta.JtaTransactionManager"/>
```

或者，你也可以将 JtaTransactionManager 传递给你的 LocalSessionFactoryBean，以强制执行相同的默认设置：
```java
<bean id="sessionFactory" class="org.springframework.orm.hibernate5.LocalSessionFactoryBean">
	<property name="dataSource" ref="dataSource"/>
	<property name="mappingResources">
		<list>
			<value>org/springframework/samples/petclinic/hibernate/petclinic.hbm.xml</value>
		</list>
	</property>
	<property name="hibernateProperties">
		<value>
			hibernate.dialect=${hibernate.dialect}
		</value>
	</property>
	<property name="jtaTransactionManager" ref="txManager"/>
</bean>

<bean id="txManager" class="org.springframework.transaction.jta.JtaTransactionManager"/>
```
# 参考文档
- [Understanding the Spring Framework Transaction Abstraction](https://docs.spring.io/spring-framework/reference/6.2/data-access/transaction/strategies.html)