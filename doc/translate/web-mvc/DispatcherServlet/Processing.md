# Processing

DispatcherServlet 处理请求的过程如下：

在请求中查找 WebApplicationContext，并将其绑定为属性，供控制器和处理流程中的其他元素使用。默认情况下，它绑定在 DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE 键下。

将 LocaleResolver 绑定到请求中，以便处理流程中的元素在处理请求时（例如渲染视图、准备数据等）可以解析出要使用的区域设置。如果不需要区域设置解析，就不需要 LocaleResolver。

将 ThemeResolver 绑定到请求中，以便视图等元素可以确定要使用哪个主题。如果不需要主题，可以忽略它。

如果指定了 multipart 文件解析器，则会检查请求中是否包含 multipart。如果包含 multipart，则会将请求包装成 MultipartHttpServletRequest，以便处理流程中的其他元素进一步处理。有关 multipart 处理的更多信息，请参阅 Multipart Resolver。

查找合适的处理器。如果找到处理器，则会运行与该处理器关联的执行链（包括预处理器、后处理器和控制器），以准备用于渲染的模型。或者，对于使用注解的控制器，可以在 HandlerAdapter 中直接渲染响应，而无需返回视图。

如果返回了模型，则会渲染视图。如果没有返回模型（可能是因为预处理器或后处理器拦截了请求，例如出于安全原因），则不会渲染视图，因为请求可能已经得到了满足。

使用 WebApplicationContext 中声明的 HandlerExceptionResolver bean 来解析请求处理过程中抛出的异常。这些异常解析器允许自定义处理异常的逻辑。有关更多信息，请参阅 Exceptions。

为了支持 HTTP 缓存，处理器可以使用 WebRequest 的 checkNotModified 方法，以及 HTTP Caching for Controllers 中描述的注解控制器的其他选项。

您可以通过在 web.xml 文件的 Servlet 声明中添加 Servlet 初始化参数（init-param 元素）来定制单个 DispatcherServlet 实例。下表列出了所支持的参数：

| 参数 | 说明 |
| --- | --- |
| contextClass | 实现 ConfigurableWebApplicationContext 的类，由该 Servlet 实例化并进行本地配置。默认情况下，使用的是 XmlWebApplicationContext。 |
|contextConfigLocation | 传递给上下文实例（由 contextClass 指定）的字符串，用于指示可在何处找到上下文。该字符串可能包含多个字符串（使用逗号作为分隔符），以支持多个上下文。如果多个上下文位置中存在重复定义的 bean，则最新的位置优先。 |
| namespace | WebApplicationContext的命名空间。默认为[servlet-name]-servlet。 |
| throwExceptionIfNoHandlerFound | 是否在未找到请求处理程序时抛出 NoHandlerFoundException。该异常可以通过 HandlerExceptionResolver（例如，通过使用带有 @ExceptionHandler 注解的控制器方法）捕获，并像处理其他异常一样进行处理。 自 6.1 版本起，此属性被设置为 true 并被标记为已弃用。 请注意，如果同时配置了默认 Servlet 处理，未解析的请求将始终转发给默认 Servlet，而不会抛出 404 错误。 |