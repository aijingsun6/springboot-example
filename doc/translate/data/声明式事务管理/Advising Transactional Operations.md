# Advising Transactional Operations

假设您希望同时执行事务操作和一些基本的性能分析建议。如何在<tx:annotation-driven/>的上下文中实现这一点？ 
当您调用updateFoo(Foo)方法时，您希望看到以下操作： 
1. 配置好的性能分析切面开始执行。 
2. 事务通知运行。 
3. 被增强对象上的方法执行。 
4. 事务提交。 
5. 性能分析切面报告整个事务方法调用的确切持续时间。

本章不打算详细解释AOP（除了与事务相关的部分）。如需详细了解AOP配置及AOP的一般概念，请参阅AOP部分。

以下代码展示了之前讨论的简单性能分析方面：

```java

package x.y;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StopWatch;
import org.springframework.core.Ordered;

public class SimpleProfiler implements Ordered {

	private int order;

	// allows us to control the ordering of advice
	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	// this method is the around advice
	public Object profile(ProceedingJoinPoint call) throws Throwable {
		Object returnValue;
		StopWatch clock = new StopWatch(getClass().getName());
		try {
			clock.start(call.toShortString());
			returnValue = call.proceed();
		} finally {
			clock.stop();
			System.out.println(clock.prettyPrint());
		}
		return returnValue;
	}
}
```
建议的排序通过 Ordered 接口进行控制。有关建议排序的完整详细信息，请参阅建议排序。

以下配置创建了一个 fooService bean，该 bean 按所需顺序应用了性能分析和事务处理方面：

```xml
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

	<bean id="fooService" class="x.y.service.DefaultFooService"/>

	<!-- this is the aspect -->
	<bean id="profiler" class="x.y.SimpleProfiler">
		<!-- run before the transactional advice (hence the lower order number) -->
		<property name="order" value="1"/>
	</bean>

	<tx:annotation-driven transaction-manager="txManager" order="200"/>

	<aop:config>
			<!-- this advice runs around the transactional advice -->
			<aop:aspect id="profilingAspect" ref="profiler">
				<aop:pointcut id="serviceMethodWithReturnValue"
						expression="execution(!void x.y..*Service.*(..))"/>
				<aop:around method="profile" pointcut-ref="serviceMethodWithReturnValue"/>
			</aop:aspect>
	</aop:config>

	<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
		<property name="driverClassName" value="oracle.jdbc.driver.OracleDriver"/>
		<property name="url" value="jdbc:oracle:thin:@rj-t42:1521:elvis"/>
		<property name="username" value="scott"/>
		<property name="password" value="tiger"/>
	</bean>

	<bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
		<property name="dataSource" ref="dataSource"/>
	</bean>

</beans>

```
你可以以类似的方式配置任意数量的其他方面。 
以下示例创建了与前两个示例相同的设置，但使用了纯XML声明式方法：


```xml
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

	<bean id="fooService" class="x.y.service.DefaultFooService"/>

	<!-- the profiling advice -->
	<bean id="profiler" class="x.y.SimpleProfiler">
		<!-- run before the transactional advice (hence the lower order number) -->
		<property name="order" value="1"/>
	</bean>

	<aop:config>
		<aop:pointcut id="entryPointMethod" expression="execution(* x.y..*Service.*(..))"/>
		<!-- runs after the profiling advice (cf. the order attribute) -->

		<aop:advisor advice-ref="txAdvice" pointcut-ref="entryPointMethod" order="2"/>
		<!-- order value is higher than the profiling aspect -->

		<aop:aspect id="profilingAspect" ref="profiler">
			<aop:pointcut id="serviceMethodWithReturnValue"
					expression="execution(!void x.y..*Service.*(..))"/>
			<aop:around method="profile" pointcut-ref="serviceMethodWithReturnValue"/>
		</aop:aspect>

	</aop:config>

	<tx:advice id="txAdvice" transaction-manager="txManager">
		<tx:attributes>
			<tx:method name="get*" read-only="true"/>
			<tx:method name="*"/>
		</tx:attributes>
	</tx:advice>

	<!-- other <bean/> definitions such as a DataSource and a TransactionManager here -->

</beans>
```
上述配置的结果是，一个名为 fooService 的 bean 按顺序应用了性能分析和事务处理方面的增强。如果你希望性能分析增强在事务处理增强之前执行（进入时），并在事务处理增强之后执行（退出时），你可以交换性能分析增强 bean 的 order 属性值，使其高于事务处理增强的 order 值。

你可以以类似的方式配置其他增强。


# 参考文档
- [Advice Ordering](https://docs.spring.io/spring-framework/reference/6.2/core/aop/ataspectj/advice.html#aop-ataspectj-advice-ordering)