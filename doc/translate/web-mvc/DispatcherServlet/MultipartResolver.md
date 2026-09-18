# Multipart Resolver

来自 `org.springframework.web.multipart` 包的 `MultipartResolver` 是一种用于解析包含文件上传的多部分请求的策略。对于 Servlet 多部分请求解析，存在一个基于容器的 `StandardServletMultipartResolver` 实现。请注意，基于 Apache Commons FileUpload 的过时 `CommonsMultipartResolver` 已不再可用，自 Spring Framework 6.0 起，其新的 Servlet 5.0+ 基线已不再支持该实现。

要启用多部分处理，您需要在 `DispatcherServlet` 的 Spring 配置中声明一个名为 `multipartResolver` 的 `MultipartResolver` bean。`DispatcherServlet` 会检测到该 bean，并将其应用于传入的请求。当接收到内容类型为 `multipart/form-data` 的 POST 请求时，解析器会解析内容，将当前的 `HttpServletRequest` 包装为 `MultipartHttpServletRequest`，以便除了将各部分作为请求参数暴露外，还能访问解析后的文件。

Servlet 多部分解析
Servlet 多部分解析需要通过 Servlet 容器配置来启用。具体方法如下：
在 Java 中，在 Servlet 注册时设置 `MultipartConfigElement`。
在 `web.xml` 中，将 `<multipart-config>` 部分添加到 Servlet 声明中。
以下示例展示了如何在 Servlet 注册时设置 `MultipartConfigElement`：

```java
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	// ...

	@Override
	protected void customizeRegistration(ServletRegistration.Dynamic registration) {

		// Optionally also set maxFileSize, maxRequestSize, fileSizeThreshold
		registration.setMultipartConfig(new MultipartConfigElement("/tmp"));
	}

}
```
一旦Servlet的多部分配置设置完成，您可以添加一个名为multipartResolver的StandardServletMultipartResolver类型的bean。 
此解析器变体直接使用Servlet容器的多部分解析器，可能会使应用程序暴露于容器实现的差异中。默认情况下，它会尝试解析任何HTTP方法下的multipart/内容类型，但这可能并非在所有Servlet容器中都受支持。有关详细信息和配置选项，请参阅StandardServletMultipartResolver的javadoc。

# 参考文档
- [StandardServletMultipartResolver](https://docs.spring.io/spring-framework/docs/6.2.19/javadoc-api/org/springframework/web/multipart/support/StandardServletMultipartResolver.html)