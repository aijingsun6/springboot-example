# 使用 @Transactional 注解

除了基于XML的声明式事务配置方法外，您还可以使用基于注解的方法。直接在Java源代码中声明事务语义，可以使声明更接近受影响的代码。这样做不会带来过多的耦合风险，因为那些需要以事务方式使用的代码几乎总是以这种方式部署的。

标准的 jakarta.transaction.Transactional 注解也支持作为 Spring 自身注解的直接替代。有关更多详细信息，请参阅 JTA 文档。

使用 @Transactional 注解所带来的便利性，可以通过一个示例得到最好的说明，该示例将在下文中进行解释。考虑以下类定义：

```java

// the service class that we want to make transactional
@Transactional
public class DefaultFooService implements FooService {

	@Override
	public Foo getFoo(String fooName) {
		// ...
	}

	@Override
	public Foo getFoo(String fooName, String barName) {
		// ...
	}

	@Override
	public void insertFoo(Foo foo) {
		// ...
	}

	@Override
	public void updateFoo(Foo foo) {
		// ...
	}
}
```
如上所述，该注解在类级别使用时，表示声明类（及其子类）中所有方法的默认事务属性。或者，也可以对每个方法单独进行注解。有关Spring认为哪些方法具有事务性的更多细节，请参阅方法可见性部分。请注意，类级别的注解不会应用于类层次结构中的祖先类；在这种情况下，继承的方法需要在子类中重新声明，才能参与子类级别的注解。

当像上面这样的POJO类在Spring上下文中定义为bean时，可以通过在@Configuration类中添加@EnableTransactionManagement注解，使该bean实例具有事务性。完整的细节请参阅javadoc文档。

在XML配置中，<tx:annotation-driven/>标签提供了类似的便利：

```java
<!-- from the file 'context.xml' -->
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

	<!-- this is the service object that we want to make transactional -->
	<bean id="fooService" class="x.y.service.DefaultFooService"/>

	<!-- enable the configuration of transactional behavior based on annotations -->
	<!-- a TransactionManager is still required -->
    // 	The line that makes the bean instance transactional.
	<tx:annotation-driven transaction-manager="txManager"/>

	<bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
		<!-- (this dependency is defined somewhere else) -->
		<property name="dataSource" ref="dataSource"/>
	</bean>

	<!-- other <bean/> definitions here -->

</beans>

```

如果您要注入的 TransactionManager 的 bean 名称为 transactionManager，则可以省略 <tx:annotation-driven/> 标签中的 transaction-manager 属性。如果您要依赖注入的 TransactionManager bean 具有其他名称，则必须使用 transaction-manager 属性，如前面的示例所示。


反应式事务方法使用反应式返回类型，与命令式编程安排形成对比，如下列表所示：

```java

// the reactive service class that we want to make transactional
@Transactional
public class DefaultFooService implements FooService {

	@Override
	public Publisher<Foo> getFoo(String fooName) {
		// ...
	}

	@Override
	public Mono<Foo> getFoo(String fooName, String barName) {
		// ...
	}

	@Override
	public Mono<Void> insertFoo(Foo foo) {
		// ...
	}

	@Override
	public Mono<Void> updateFoo(Foo foo) {
		// ...
	}
}
```

请注意，返回的 Publisher 在响应式流取消信号方面有特殊考虑。有关更多详细信息，请参阅“使用 TransactionalOperator”下的“取消信号”部分。

代理模式下的@Transactional注解与方法可见性
@Transactional注解通常用于具有public可见性的方法。从6.0版本开始，基于类的代理默认也支持protected或包可见性的方法。请注意，基于接口的代理中的事务方法必须始终为public，且必须在被代理的接口中定义。对于这两种代理，只有通过代理传入的外部方法调用才会被拦截。

如果您希望在不同类型的代理之间对方法可见性保持一致的处理方式（这是5.3版本之前的默认行为），请考虑指定publicMethodsOnly：

```java

/**
 * Register a custom AnnotationTransactionAttributeSource with the
 * publicMethodsOnly flag set to true to consistently ignore non-public methods.
 * @see ProxyTransactionManagementConfiguration#transactionAttributeSource()
 */
@Bean
TransactionAttributeSource transactionAttributeSource() {
	return new AnnotationTransactionAttributeSource(true);
}
```


你可以将 `@Transactional` 注解应用于接口定义、接口方法、类定义或类方法。然而，仅仅存在 `@Transactional` 注解本身并不足以激活事务行为。`@Transactional` 注解只是元数据，可以被相应的运行时基础设施所消费，这些基础设施利用该元数据来配置具有事务行为的适当 Bean。在前面的示例中，`<tx:annotation-driven/>` 元素在运行时开启了实际的事务管理。

Spring 团队建议，你应该将 `@Transactional` 注解应用于具体类的方法，而不是依赖接口中的注解方法，即使从 Spring 5.0 开始，接口代理和目标类代理确实可以处理接口中的注解方法。由于 Java 注解不会从接口继承，因此在使用 AspectJ 模式时，接口中声明的注解仍然不会被织入基础设施识别，所以切面不会被应用。因此，你的事务注解可能会被静默地忽略：你的代码可能看起来“正常工作”，直到你测试回滚场景时才会发现问题。

在代理模式（默认模式）下，只有通过代理传入的外部方法调用才会被拦截。这意味着，即使被调用的方法被标记了 `@Transactional`，自调用（实际上就是目标对象内部的一个方法调用目标对象的另一个方法）也不会在运行时产生实际的事务。此外，代理必须完全初始化才能提供预期的行为，因此你不应该在初始化代码中依赖这一特性——例如，在 `@PostConstruct` 方法中。

如果你希望自调用也能被事务包装，可以考虑使用 AspectJ 模式（参见下表中的 `mode` 属性）。在这种情况下，首先并不存在代理。相反，目标类会被织入（即修改其字节码），以支持在任何类型的方法上实现 `@Transactional` 的运行时行为。


在评估方法的事务设置时，最具体的设置具有优先权。在以下示例中，DefaultFooService 类在类级别被注解为只读事务设置，但同一类中 updateFoo(Foo) 方法上的 @Transactional 注解优先于类级别定义的事务设置。

```java

@Transactional(readOnly = true)
public class DefaultFooService implements FooService {

	public Foo getFoo(String fooName) {
		// ...
	}

	// these settings have precedence for this method
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void updateFoo(Foo foo) {
		// ...
	}
}
```

# @Transactional 设置 
@Transactional 注解是一种元数据，用于指定某个接口、类或方法必须具有事务语义（例如，“当调用此方法时，启动一个全新的只读事务，并挂起任何现有事务”）。默认的 @Transactional 设置如下： 
传播设置是 PROPAGATION_REQUIRED。 
隔离级别是 ISOLATION_DEFAULT。 
事务为读写模式。 
事务超时默认使用底层事务系统的默认超时时间；如果不支持超时，则无超时限制。 
任何 RuntimeException 或 Error 会触发回滚，而任何受检异常不会触发回滚。 
您可以更改这些默认设置。下表总结了 @Transactional 注解的各个属性：

...


有关回滚规则的语义、模式以及基于模式的回滚规则可能意外匹配的警告的更多详细信息，请参阅回滚规则。

从6.2版本开始，您可以全局更改默认的回滚行为——例如通过 `@EnableTransactionManagement(rollbackOn=ALL_EXCEPTIONS)`，这将导致在事务中引发的所有异常（包括任何受检异常）都会触发回滚。如需进一步自定义，`AnnotationTransactionAttributeSource` 提供了 `addDefaultRollbackRule(RollbackRuleAttribute)` 方法，用于添加自定义的默认规则。

请注意，特定事务的回滚规则会覆盖默认行为，但对于未指定的异常仍保留所选的默认设置。Spring 的 `@Transactional` 注解以及 JTA 的 `jakarta.transaction.Transactional` 注解均遵循此规则。

除非您依赖具有提交行为的 EJB 风格业务异常，否则建议切换到 `ALL_EXCEPTIONS`，以确保即使在（可能意外的）受检异常情况下也能保持一致的回滚语义。此外，对于基于 Kotlin 的应用程序（其中完全不强制执行受检异常），也建议进行此切换。

目前，您无法对事务的名称进行显式控制，这里的“名称”指的是在事务监视器和日志输出中显示的事务名称。对于声明式事务，事务名称始终是事务代理类的完全限定类名 + “。” + 方法名。例如，如果 `BusinessService` 类的 `handlePayment(..)` 方法启动了一个事务，那么该事务的名称将是 `com.example.BusinessService.handlePayment`。


# 多个事务管理器使用 @Transactional

大多数 Spring 应用程序只需要一个事务管理器，但在某些情况下，您可能希望在单个应用程序中使用多个独立的事务管理器。您可以使用 `@Transactional` 注解的 `value` 或 `transactionManager` 属性来可选地指定要使用的事务管理器的标识。这可以是事务管理器 bean 的 bean 名称或限定符值。例如，使用限定符表示法，您可以将以下 Java 代码与应用程序上下文中以下事务管理器 bean 的声明结合起来：

```java

public class TransactionalService {

	@Transactional("order")
	public void setSomething(String name) { ... }

	@Transactional("account")
	public void doSomething() { ... }

	@Transactional("reactive-account")
	public Mono<Void> doSomethingReactive() { ... }
}
```
```xml
<tx:annotation-driven/>

	<bean id="transactionManager1" class="org.springframework.jdbc.support.JdbcTransactionManager">
		...
		<qualifier value="order"/>
	</bean>

	<bean id="transactionManager2" class="org.springframework.jdbc.support.JdbcTransactionManager">
		...
		<qualifier value="account"/>
	</bean>

	<bean id="transactionManager3" class="org.springframework.data.r2dbc.connection.R2dbcTransactionManager">
		...
		<qualifier value="reactive-account"/>
	</bean>
```
在此情况下，`TransactionalService` 上的各个方法会在独立的 `TransactionManager` 下运行，这些 `TransactionManager` 通过 `order`、`account` 和 `reactive-account` 限定符进行区分。如果未找到具有特定限定符的 `TransactionManager` Bean，则会继续使用默认的 `<tx:annotation-driven>` 目标 Bean 名称 `transactionManager`。

如果同一类中的所有事务方法共享相同的限定符，可以考虑声明一个类型级别的 `org.springframework.beans.factory.annotation.Qualifier` 注解。如果该注解的值与特定 `TransactionManager` 的限定符值（或 Bean 名称）匹配，则该 `TransactionManager` 将被用于事务定义，而无需在 `@Transactional` 注解本身上指定特定限定符。

这种类型级别的限定符可以声明在具体类上，同样适用于基类中的事务定义。这实际上会覆盖任何未限定基类方法的默认事务管理器选择。

最后但同样重要的是，这种类型级别的 Bean 限定符可以用于多种目的。例如，如果其值为 "order"，它既可以用于自动装配（标识订单仓库），也可以用于事务管理器选择，前提是自动装配的目标 Bean 以及关联的事务管理器定义都声明了相同的限定符值。该限定符值只需在类型匹配的 Bean 集合中唯一即可，无需作为 ID 使用。

# 自定义组合注解
如果你发现自己在许多不同的方法上反复使用相同的 @Transactional 属性，Spring 的元注解支持允许你为特定用例定义自定义的组合注解。例如，考虑以下注解定义：

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(transactionManager = "order", label = "causal-consistency")
public @interface OrderTx {
}

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(transactionManager = "account", label = "retryable")
public @interface AccountTx {
}

```

前面的注解使我们能够将上一节的示例编写如下：

```java

public class TransactionalService {

	@OrderTx
	public void setSomething(String name) {
		// ...
	}

	@AccountTx
	public void doSomething() {
		// ...
	}
}
```
在前面的示例中，我们使用了语法来定义事务管理器限定符和事务标签，但我们也可以包含传播行为、回滚规则、超时设置以及其他特性。

# 参考文档
- [Using @Transactional](https://docs.spring.io/spring-framework/reference/6.2/data-access/transaction/declarative/annotations.html)
- [Rollback rules](https://docs.spring.io/spring-framework/reference/6.2/data-access/transaction/declarative/rolling-back.html#transaction-declarative-rollback-rules)