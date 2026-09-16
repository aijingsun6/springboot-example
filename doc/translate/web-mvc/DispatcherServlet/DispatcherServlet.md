# DispatcherServlet

与许多其他Web框架一样，Spring MVC的设计基于前端控制器模式，其中中央Servlet（即DispatcherServlet）提供请求处理的共享算法，而实际工作则由可配置的委托组件执行。该模型具有灵活性，支持多种工作流程。

与任何Servlet一样，DispatcherServlet需要通过Java配置或在web.xml中根据Servlet规范进行声明和映射。反过来，DispatcherServlet使用Spring配置来发现其所需的委托组件，用于请求映射(request mapping)、视图解析( view resolution)、异常处理等(exception handling)。

以下Java配置示例注册并初始化了DispatcherServlet，该Servlet由Servlet容器自动检测（参见Servlet配置）：

```java
public class MyWebApplicationInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) {

		// Load Spring web application configuration
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(AppConfig.class);

		// Create and register the DispatcherServlet
		DispatcherServlet servlet = new DispatcherServlet(context);
		ServletRegistration.Dynamic registration = servletContext.addServlet("app", servlet);
		registration.setLoadOnStartup(1);
		registration.addMapping("/app/*");
	}
}
```
除了直接使用 ServletContext API 外，您还可以扩展 AbstractAnnotationConfigDispatcherServletInitializer 并重写特定方法（参见上下文层次结构下的示例）。

对于程序化使用场景，可以使用 GenericWebApplicationContext 作为 AnnotationConfigWebApplicationContext 的替代方案。详情请参见 GenericWebApplicationContext 的 JavaDoc。

以下是一个 web.xml 配置示例，用于注册并初始化 DispatcherServlet：

```xml

<web-app>

	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>/WEB-INF/app-context.xml</param-value>
	</context-param>

	<servlet>
		<servlet-name>app</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value></param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>

	<servlet-mapping>
		<servlet-name>app</servlet-name>
		<url-pattern>/app/*</url-pattern>
	</servlet-mapping>

</web-app>
```


Spring Boot 遵循不同的初始化顺序。它不依赖于 Servlet 容器的生命周期，而是通过 Spring 配置来引导自身和嵌入式的 Servlet 容器。过滤器和 Servlet 的声明会在 Spring 配置中被检测到，并注册到 Servlet 容器中。更多详细信息，请参阅 Spring Boot 文档。



# 参考文档
- [GenericWebApplicationContext](https://docs.spring.io/spring-framework/docs/6.2.19/javadoc-api/org/springframework/web/context/support/GenericWebApplicationContext.html)
- [spring boot Servlet Web Applications](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.embedded-container)