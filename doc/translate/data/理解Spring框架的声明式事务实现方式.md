# 理解 Spring 框架的声明式事务实现方式

Understanding the Spring Framework’s Declarative Transaction Implementation

仅仅告诉你要在类上添加 `@Transactional` 注解，并在配置中添加 `@EnableTransactionManagement`，然后期望你理解其工作原理是不够的。为了提供更深入的理解，本节将在事务相关问题的背景下，解释 Spring 框架声明式事务基础设施的内部工作机制。

关于 Spring 框架的声明式事务支持，需要掌握的最重要概念是：该支持是通过 AOP 代理实现的，并且事务通知是由元数据（目前是基于 XML 或注解的）驱动的。AOP 与事务元数据的结合，产生了一个 AOP 代理，该代理使用 `TransactionInterceptor` 并结合适当的 `TransactionManager` 实现，来驱动方法调用周围的事务。

Spring Framework 的 TransactionInterceptor 为命令式和响应式编程模型提供事务管理。该拦截器通过检查方法的返回类型来确定所需的事务管理方式。返回响应式类型（如 Publisher 或 Kotlin Flow，或其子类型）的方法适用于响应式事务管理。所有其他返回类型（包括 void）均使用命令式事务管理的代码路径。

事务管理方式决定了所需的事务管理器。命令式事务需要 PlatformTransactionManager，而响应式事务则使用 ReactiveTransactionManager 实现。

@Transactional 通常与由 PlatformTransactionManager 管理的线程绑定事务一起使用，它将事务暴露给当前执行线程中的所有数据访问操作。注意：此方法不会传播到方法内部新启动的线程中。

由 ReactiveTransactionManager 管理的反应式事务使用 Reactor 上下文，而不是线程本地属性。因此，所有参与的数据访问操作都需要在同一个反应式管道中的相同 Reactor 上下文内执行。

当配置了 ReactiveTransactionManager 时，所有事务边界方法都应返回一个反应式管道。空方法或常规返回类型需要与常规的 PlatformTransactionManager 关联，例如，可以通过相应 @Transactional 声明中的 transactionManager 属性来关联。

下图展示了在事务代理上调用方法的概念视图。

![](./images/image.png)