# AOP

# 1.Concepts

主要有以下几个概念/术语:
- 切面(Aspect)
- 连接点(Join point):程序执行过程中的一个点，例如方法的执行或异常的处理。在Spring AOP中，连接点始终表示方法的执行。
- 建议（Advice）：在特定连接点处由某方面采取的行动。不同类型的建议包括“环绕”、“前置”和“后置”建议。（建议类型将在后续讨论。）许多面向切面编程（AOP）框架，包括Spring，都将建议建模为拦截器，并在连接点周围维护一个拦截器链。
- 切点（Pointcut）：一个用于匹配连接点的谓词。通知（Advice）与切点表达式相关联，并在切点匹配的任何连接点处执行（例如，执行某个特定名称的方法）。切点表达式所匹配的连接点概念是面向切面编程（AOP）的核心，而Spring默认使用AspectJ的切点表达式语言。
- Introduction
- 目标对象(Target object)：一个被一个或多个切面所通知的对象。也称为“被通知对象”。由于 Spring AOP 是通过运行时代理实现的，因此该对象始终是一个代理对象。
- AOP proxy:由AOP框架创建的对象，用于实现方面契约（如通知方法的执行等）。在Spring框架中，AOP代理可以是JDK动态代理或CGLIB代理。
- 织入（Weaving）：将方面与其他应用类型或对象关联起来，以创建一个被通知的对象。织入可以在编译时（例如使用 AspectJ 编译器）、加载时或运行时完成。Spring AOP 与其他纯 Java AOP 框架一样，在运行时执行织入。

## 1.1 Advice的分类
- Before advice
- After returning advice
- After throwing advice
- After (finally) advice
- Around advice

目前spring只支持bean的advice

Spring AOP currently supports only method execution join points (advising the execution of methods on Spring beans)

# 2 AOP Proxies
Spring AOP 默认使用标准的 JDK 动态代理来实现 AOP 代理。这使得任何接口（或一组接口）都可以被代理。Spring AOP 也可以使用 CGLIB 代理。当需要代理类而不是接口时，就需要使用 CGLIB。默认情况下，如果业务对象没有实现接口，Spring AOP 会使用 CGLIB。由于面向接口编程（而不是面向类编程）是一种良好的编程实践，因此业务类通常会实现一个或多个业务接口。在某些（希望是罕见的）情况下，你可能需要代理一个未在接口中声明的方法，或者需要将代理对象作为具体类型传递给某个方法，这时可以强制使用 CGLIB。

理解 Spring AOP 是基于代理的这一点非常重要。请参阅《理解 AOP 代理》以深入了解这一实现细节的真正含义。

## 2.1 Proxying Mechanisms

```
public class Main {

	public static void main(String[] args) {
		ProxyFactory factory = new ProxyFactory(new SimplePojo());
		factory.addInterface(Pojo.class);
		factory.addAdvice(new RetryAdvice());

		Pojo pojo = (Pojo) factory.getProxy();
		// this is a method call on the proxy!
		pojo.foo();
	}
}
```

# 3. @AspectJ support

## 3.1 开启 @AspectJ
```
@Configuration
@EnableAspectJAutoProxy
public class ApplicationConfiguration {
}
```
```
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xmlns:aop="http://www.springframework.org/schema/aop"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans
			https://www.springframework.org/schema/beans/spring-beans.xsd
			http://www.springframework.org/schema/aop
			https://www.springframework.org/schema/aop/spring-aop.xsd">

	<aop:aspectj-autoproxy />
</beans>
```

## 3.2 申明AspectJ
java
```
public class ApplicationConfiguration {

	@Bean
	public NotVeryUsefulAspect myAspect() {
		NotVeryUsefulAspect myAspect = new NotVeryUsefulAspect();
		// Configure properties of the aspect here
		return myAspect;
	}
}

@Aspect
public class NotVeryUsefulAspect {
}
```
xml
```xml

<bean id="myAspect" class="org.springframework.docs.core.aop.ataspectj.aopataspectj.NotVeryUsefulAspect">
	<!-- configure properties of the aspect here -->
</bean>
```
## 3.3 申明切入点(Pointcut)
一个示例有助于清楚地区分切点签名和切点表达式。以下示例定义了一个名为 anyOldTransfer 的切点，该切点匹配任何名为 transfer 的方法的执行：
```

@Aspect
public class NotVeryUsefulAspect {
    @Pointcut("execution(* transfer(..))") // the pointcut expression
    private void anyOldTransfer() {} // the pointcut signature  
}

```
### 3.3.1 支持的切入点(Pointcut)指示符
- execution:用于匹配方法执行连接点。这是在使用 Spring AOP 时主要使用的切入点指示符。
- within:限制匹配到特定类型内的连接点（在使用 Spring AOP 时，执行声明在匹配类型内的方法）。
- this:限制匹配到连接点（在使用 Spring AOP 时方法的执行），其中 bean 引用（Spring AOP 代理）是给定类型的实例。
- target:限制匹配到连接点（在使用 Spring AOP 时方法的执行），其中目标对象（被代理的应用程序对象）是给定类型的实例。
- args:限制匹配到连接点（在使用 Spring AOP 时方法的执行），其中参数是给定类型的实例。
- @target:限制匹配到连接点（在使用 Spring AOP 时方法的执行），其中执行对象的类具有给定类型的注解。
- @args:限制匹配到连接点（在使用 Spring AOP 时方法的执行），其中实际传入参数的运行时类型具有给定类型的注解。
- @within:限制匹配到具有给定注解的类型中的连接点（在使用 Spring AOP 时，执行声明在具有给定注解的类型中的方法）。
- @annotation:限制匹配到连接点，其中连接点的主体（Spring AOP 中正在运行的方法）具有给定的注解。

### 3.3.2 切入点的组合
使用符合 &&, || , ! 进行组合
```java
package com.xyz;

public class Pointcuts {

	@Pointcut("execution(public * *(..))")
	public void publicMethod() {}

	@Pointcut("within(com.xyz.trading..*)")
	public void inTrading() {}

	@Pointcut("publicMethod() && inTrading()")
	public void tradingOperation() {}
}
```
如上所示，将更复杂的切入点表达式构建为较小的命名切入点是一种最佳实践。通过名称引用切入点时，普通的 Java 可见性规则同样适用（你可以在同一类型中看到私有切入点，在继承层次结构中看到受保护切入点，在任何地方看到公共切入点，等等）。可见性不会影响切入点的匹配。
### 3.3.3 共享命名切入点定义

在开发企业级应用程序时，开发人员通常需要从多个方面引用应用程序的模块和特定的操作集。为此，我们建议定义一个专用类，用于封装常用的命名切入点表达式

```java
package com.xyz;

import org.aspectj.lang.annotation.Pointcut;

public class CommonPointcuts {

	/**
	 * A join point is in the web layer if the method is defined
	 * in a type in the com.xyz.web package or any sub-package
	 * under that.
	 */
	@Pointcut("within(com.xyz.web..*)")
	public void inWebLayer() {}

	/**
	 * A join point is in the service layer if the method is defined
	 * in a type in the com.xyz.service package or any sub-package
	 * under that.
	 */
	@Pointcut("within(com.xyz.service..*)")
	public void inServiceLayer() {}

	/**
	 * A join point is in the data access layer if the method is defined
	 * in a type in the com.xyz.dao package or any sub-package
	 * under that.
	 */
	@Pointcut("within(com.xyz.dao..*)")
	public void inDataAccessLayer() {}

	/**
	 * A business service is the execution of any method defined on a service
	 * interface. This definition assumes that interfaces are placed in the
	 * "service" package, and that implementation types are in sub-packages.
	 *
	 * If you group service interfaces by functional area (for example,
	 * in packages com.xyz.abc.service and com.xyz.def.service) then
	 * the pointcut expression "execution(* com.xyz..service.*.*(..))"
	 * could be used instead.
	 *
	 * Alternatively, you can write the expression using the 'bean'
	 * PCD, like so "bean(*Service)". (This assumes that you have
	 * named your Spring service beans in a consistent fashion.)
	 */
	@Pointcut("execution(* com.xyz..service.*.*(..))")
	public void businessService() {}

	/**
	 * A data access operation is the execution of any method defined on a
	 * DAO interface. This definition assumes that interfaces are placed in the
	 * "dao" package, and that implementation types are in sub-packages.
	 */
	@Pointcut("execution(* com.xyz.dao.*.*(..))")
	public void dataAccessOperation() {}

}
```

### 3.3.4 execution 
语法：
```
execution(modifiers-pattern?
			ret-type-pattern
			declaring-type-pattern?name-pattern(param-pattern)
			throws-pattern?)

```
- 除了ret-type-pattern，name-pattern，param-pattern其余都是可选的
- ret-type-pattern 返回类型匹配， '*',表示匹配任何类型的返回
- name pattern 方法名称匹配：，* 表示匹配任何方法
- param-pattern 参数类型匹配，()，匹配无参函数，(..)匹配任何数量的参数，```(*)``` 一个任何类型的参数，```(*,String)``` 匹配入参个数=2，第二个参数类型是String

常见的pointcut

- ```execution(public * *(..))``` 匹配任何public方法
- ``` execution(* set*(..))``` 匹配任何方法名称以set开头的方法
- ``` execution(* com.xyz.service.AccountService.*(..))  ```  匹配 AccountService的任何方法
-  ``` execution(* com.xyz.service.*.*(..)) ``` 匹配com.xyz.service包下的任何类的任何方法
-  ``` execution(* com.xyz.service..*.*(..))  ``` 匹配 com.xyz.service包下 或子包的任何类的任何方法
-  ```  within(com.xyz.service.*) ``` com.xyz.service下的所有方法，子包不生效
- ```within(com.xyz.service..*) ``` com.xyz.service下的所有方法,子包也生效
- ```this(com.xyz.service.AccountService) ```

### 3.3.5 编写良好的切入点（Pointcuts）
一个编写良好的切入点至少应包含前两种类型（种类限定符和作用域限定符）。你也可以加入上下文限定符，以便根据连接点的上下文进行匹配，或者将该上下文绑定到通知中以供使用。仅提供种类限定符或仅提供上下文限定符虽然可行，但由于需要额外的处理和分析，可能会影响织入性能（时间和内存消耗）。作用域限定符的匹配速度非常快，使用它们意味着 AspectJ 可以迅速排除那些无需进一步处理的连接点组。因此，一个良好的切入点应尽可能包含一个作用域限定符

## 3.4 Advice
建议（Advice）与一个切入点表达式相关联，并在该切入点匹配的方法执行之前、之后或环绕执行时运行。切入点表达式可以是内联切入点，也可以是对命名切入点的引用。

### 3.4.1 Before
```java

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BeforeExample {

	@Before("execution(* com.xyz.dao.*.*(..))")
	public void doAccessCheck() {
		// ...
	}
}

或者使用名称PointCut
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BeforeExample {

	@Before("com.xyz.CommonPointcuts.dataAccessOperation()")
	public void doAccessCheck() {
		// ...
	}
}
```

### 3.4.2 AfterReturning
```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;

@Aspect
public class AfterReturningExample {

	@AfterReturning("execution(* com.xyz.dao.*.*(..))")
	public void doAccessCheck() {
		// ...
	}
}

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;

@Aspect
public class AfterReturningExample {

	@AfterReturning(
		pointcut="execution(* com.xyz.dao.*.*(..))",
		returning="retVal")
	public void doAccessCheck(Object retVal) {
		// ...
	}
}

```
### 3.4.3 AfterThrowing
```

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterThrowing;

@Aspect
public class AfterThrowingExample {

	@AfterThrowing("execution(* com.xyz.dao.*.*(..))")
	public void doRecoveryActions() {
		// ...
	}
}

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterThrowing;

@Aspect
public class AfterThrowingExample {

	@AfterThrowing(
		pointcut="execution(* com.xyz.dao.*.*(..))",
		throwing="ex")
	public void doRecoveryActions(DataAccessException ex) {
		// ...
	}
}
```
### 3.4.5 After (Finally) Advice
可以用作释放资源

```java

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;

@Aspect
public class AfterFinallyExample {

	@After("execution(* com.xyz.dao.*.*(..))")
	public void doReleaseLock() {
		// ...
	}
}
```
### 3.4.6 Around Advice
```java

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;

@Aspect
public class AroundExample {

	@Around("execution(* com.xyz..service.*.*(..))")
	public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
		// start stopwatch
		Object retVal = pjp.proceed();
		// stop stopwatch
		return retVal;
	}
}
```
### 3.4.7 Advice参数
第一个参数的类型总是 ProceedingJoinPoint

#### 3.4.7.1 使用 args 来绑定参数

```java
@Before("execution(* com.xyz.dao.*.*(..)) && args(account,..)")
public void validateAccount(Account account) {
	// ...
}
// 或者使用名称切面
@Pointcut("execution(* com.xyz.dao.*.*(..)) && args(account,..)")
private void accountDataAccessOperation(Account account) {}

@Before("accountDataAccessOperation(account)")
public void validateAccount(Account account) {
	// ...
}
```
#### 3.4.7.2 使用 @annotation 来绑定参数
```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
	AuditCode value();
}

@Before("com.xyz.Pointcuts.publicMethod() && @annotation(auditable)")
public void audit(Auditable auditable) {
	AuditCode code = auditable.value();
	// ...
}

```
#### 3.4.7.3 Advice 泛型参数
```java
public interface Sample<T> {
	void sampleGenericMethod(T param);
	void sampleGenericCollectionMethod(Collection<T> param);
}

@Before("execution(* ..Sample+.sampleGenericMethod(*)) && args(param)")
public void beforeSampleMethod(MyType param) {
	// Advice implementation
}
```
#### 3.4.7.4 显式绑定参数
```java

@Before(
	value = "com.xyz.Pointcuts.publicMethod() && target(bean) && @annotation(auditable)",
	argNames = "bean,auditable")
public void audit(Object bean, Auditable auditable) {
	AuditCode code = auditable.value();
	// ... use code and bean
}
```



# 参考文档
- [spring-core-aop-6.2](https://docs.spring.io/spring-framework/reference/6.2/core/aop.html)
- [AspectJ project](https://eclipse.dev/aspectj/)
- [The  AspectJ Programming Guide](https://eclipse.dev/aspectj/doc/released/progguide/index.html)
- [The AspectJ 5 Development Kit Developer's Notebook](https://eclipse.dev/aspectj/doc/released/adk15notebook/index.html)