# 申明(Declaration)

您可以通过在Servlet的WebApplicationContext中使用标准的Spring bean定义来定义控制器bean。@Controller注解类型支持自动检测，这与Spring在类路径中检测@Component类并自动注册其bean定义的通用支持保持一致。它同时也作为被注解类的注解类型，表明该类作为Web组件的角色。

要启用对此类@Controller bean的自动检测，您可以在Java配置中添加组件扫描，如下例所示：
```java

@Configuration
@ComponentScan("org.example.web")
public class WebConfiguration {

	// ...
}
```

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xmlns:context="http://www.springframework.org/schema/context"
	   xsi:schemaLocation="
			http://www.springframework.org/schema/beans
			https://www.springframework.org/schema/beans/spring-beans.xsd
			http://www.springframework.org/schema/context
			https://www.springframework.org/schema/context/spring-context.xsd">

	<context:component-scan base-package="org.example.web"/>

	<!-- ... -->

</beans>

```


@RestController 是一个组合注解，它本身被元注解 @Controller 和 @ResponseBody 标注，用于指示一个控制器，该控制器的每个方法都继承了类型级别的 @ResponseBody 注解，因此会直接将数据写入响应体，而不是通过 HTML 模板进行视图解析和渲染。


# AOP Proxies

在某些情况下，您可能需要在运行时用AOP代理装饰一个控制器。例如，如果您选择在控制器上直接使用@Transactional注解。在这种情况下，对于控制器而言，我们建议使用基于类的代理。如果注解直接作用于控制器，这种情况会自动发生。

如果控制器实现了某个接口，并且需要AOP代理，您可能需要显式配置基于类的代理。例如，在使用@EnableTransactionManagement时，您可以将其更改为@EnableTransactionManagement(proxyTargetClass = true)；在使用<tx:annotation-driven/>时，您可以将其更改为<tx:annotation-driven proxy-target-class="true"/>。

请注意，从6.0版本开始，由于接口代理机制的引入，Spring MVC不再仅根据接口上的类型级@RequestMapping注解来检测控制器。请启用基于类的代理，否则接口也必须同时具有@Controller注解。