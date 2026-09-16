# Servlet 配置

在Servlet环境中，您可以选择以编程方式配置Servlet容器，作为web.xml文件的替代方案或与之结合使用。以下示例注册了一个DispatcherServlet。


```java

import org.springframework.web.WebApplicationInitializer;

public class MyWebApplicationInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext container) {
		XmlWebApplicationContext appContext = new XmlWebApplicationContext();
		appContext.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");

		ServletRegistration.Dynamic registration = container.addServlet("dispatcher", new DispatcherServlet(appContext));
		registration.setLoadOnStartup(1);
		registration.addMapping("/");
	}
}
```

`WebApplicationInitializer` 是 Spring MVC 提供的一个接口，它确保您的实现能够被检测到，并自动用于初始化任何 Servlet 3 容器。一个名为 `AbstractDispatcherServletInitializer` 的抽象基类实现了 `WebApplicationInitializer`，通过重写方法来指定 Servlet 映射和 DispatcherServlet 配置的位置，可以更轻松地注册 DispatcherServlet。 
对于使用基于 Java 的 Spring 配置的应用程序，推荐使用这种方式，如下例所示：

```java


public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return null;
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] { MyWebConfig.class };
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}
}
```
如果你使用基于 XML 的 Spring 配置，你应该直接从 AbstractDispatcherServletInitializer 继承，如下例所示：

```java
public class MyWebAppInitializer extends AbstractDispatcherServletInitializer {

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		return null;
	}

	@Override
	protected WebApplicationContext createServletApplicationContext() {
		XmlWebApplicationContext cxt = new XmlWebApplicationContext();
		cxt.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");
		return cxt;
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}
}

```

AbstractDispatcherServletInitializer 还提供了一种便捷的方式来添加 Filter 实例，并使它们自动映射到 DispatcherServlet，如下例所示：

```java

public class MyWebAppInitializer extends AbstractDispatcherServletInitializer {

	// ...

	@Override
	protected Filter[] getServletFilters() {
		return new Filter[] {
			new HiddenHttpMethodFilter(), new CharacterEncodingFilter() };
	}
}
```
每个过滤器都会根据其实际类型添加一个默认名称，并自动映射到 DispatcherServlet。
AbstractDispatcherServletInitializer 的 isAsyncSupported 受保护方法提供了一个统一的位置，用于在 DispatcherServlet 及其映射的所有过滤器上启用异步支持。默认情况下，此标志设置为 true。
最后，如果您需要进一步自定义 DispatcherServlet 本身，可以重写 createDispatcherServlet 方法。


