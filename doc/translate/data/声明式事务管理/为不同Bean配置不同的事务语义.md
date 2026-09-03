# 为不同 Bean 配置不同的事务语义


考虑这样一种场景：你拥有多个服务层对象，并且希望对每个对象应用完全不同的事务配置。你可以通过定义具有不同切入点（pointcut）和建议引用（advice-ref）属性值的独立 <aop:advisor/> 元素来实现这一点。

作为对比，首先假设你所有的服务层类都定义在根包 x.y.service 中。为了让所有属于该包（或其子包）中定义的类，并且名称以 Service 结尾的 bean 都采用默认的事务配置，你可以编写如下代码：

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

	<aop:config>

		<aop:pointcut id="serviceOperation"
				expression="execution(* x.y.service..*Service.*(..))"/>

		<aop:advisor pointcut-ref="serviceOperation" advice-ref="txAdvice"/>

	</aop:config>

	<!-- these two beans will be transactional... -->
	<bean id="fooService" class="x.y.service.DefaultFooService"/>
	<bean id="barService" class="x.y.service.extras.SimpleBarService"/>

	<!-- ... and these two beans won't -->
	<bean id="anotherService" class="org.xyz.SomeService"/> <!-- (not in the right package) -->
	<bean id="barManager" class="x.y.service.SimpleBarManager"/> <!-- (doesn't end in 'Service') -->

	<tx:advice id="txAdvice">
		<tx:attributes>
			<tx:method name="get*" read-only="true"/>
			<tx:method name="*"/>
		</tx:attributes>
	</tx:advice>

	<!-- other transaction infrastructure beans such as a TransactionManager omitted... -->

</beans>

```
以下示例展示了如何配置两个具有完全不同的事务设置的不同bean：

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

	<aop:config>

		<aop:pointcut id="defaultServiceOperation"
				expression="execution(* x.y.service.*Service.*(..))"/>

		<aop:pointcut id="noTxServiceOperation"
				expression="execution(* x.y.service.ddl.DefaultDdlManager.*(..))"/>

		<aop:advisor pointcut-ref="defaultServiceOperation" advice-ref="defaultTxAdvice"/>

		<aop:advisor pointcut-ref="noTxServiceOperation" advice-ref="noTxAdvice"/>

	</aop:config>

	<!-- this bean will be transactional (see the 'defaultServiceOperation' pointcut) -->
	<bean id="fooService" class="x.y.service.DefaultFooService"/>

	<!-- this bean will also be transactional, but with totally different transactional settings -->
	<bean id="anotherFooService" class="x.y.service.ddl.DefaultDdlManager"/>

	<tx:advice id="defaultTxAdvice">
		<tx:attributes>
			<tx:method name="get*" read-only="true"/>
			<tx:method name="*"/>
		</tx:attributes>
	</tx:advice>

	<tx:advice id="noTxAdvice">
		<tx:attributes>
			<tx:method name="*" propagation="NEVER"/>
		</tx:attributes>
	</tx:advice>

	<!-- other transaction infrastructure beans such as a TransactionManager omitted... -->

</beans>
```

# 参考文档
- [Configuring Different Transactional Semantics for Different Beans](https://docs.spring.io/spring-framework/reference/6.2/data-access/transaction/declarative/diff-tx.html)