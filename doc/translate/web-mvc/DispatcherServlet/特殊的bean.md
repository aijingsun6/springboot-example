# 特殊的bean(Special Bean Types)


DispatcherServlet 会将请求委托给特殊的 Bean 来处理，并生成相应的响应。所谓“特殊的 Bean”，指的是由 Spring 管理的、实现了框架契约的对象实例。这些 Bean 通常带有内置的契约，但你也可以自定义其属性，或对其进行扩展或替换。

下表列出了 DispatcherServlet 检测到的特殊 Bean：


# HandlerMapping

将请求映射到处理器，并附带一个用于预处理和后处理的拦截器列表。该映射基于某些标准，具体细节因 `HandlerMapping` 的实现而异。 
主要的两种 `HandlerMapping` 实现是 `RequestMappingHandlerMapping`（支持带有 `@RequestMapping` 注解的方法）和 `SimpleUrlHandlerMapping`（维护 URI 路径模式到处理器的显式注册）。


# HandlerAdapter

帮助 DispatcherServlet 调用与请求映射的处理器，无论该处理器实际是如何被调用的。例如，调用一个带有注解的控制器需要解析注解。HandlerAdapter 的主要目的是将 DispatcherServlet 与这些细节隔离开来。

# HandlerExceptionResolver

解决异常的策略，可能将其映射到处理程序、HTML错误视图或其他目标。参见“异常”。

# ViewResolver

将处理器返回的基于逻辑字符串的视图名称解析为用于渲染响应的实际视图。请参阅视图解析和视图技术。


# LocaleResolver, LocaleContextResolver

解析客户端正在使用的区域设置（Locale）以及可能的时区，以便能够提供国际化的视图。参见 Locale。


# ThemeResolver

确定您的 Web 应用程序可以使用的主题——例如，用于提供个性化布局。请参阅主题。

# MultipartResolver

用于解析多部分请求（例如，浏览器表单文件上传）的抽象，借助某些多部分解析库实现。参见多部分解析器。

# FlashMapManager

存储并检索可用于在请求之间（通常是在重定向过程中）传递属性的“输入”和“输出”FlashMap。参见Flash属性。



# 参考文档
- [Interception](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-servlet/handlermapping-interceptor.html)
- [Exceptions](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-servlet/exceptionhandlers.html)
- [View Resolution](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-servlet/viewresolver.html)
- [View Technologies](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc-view.html)
- [Locale](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-servlet/localeresolver.html)
- [Themes](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-servlet/themeresolver.html)
- [Multipart Resolver](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-servlet/multipart.html)
- [Flash Attributes](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-methods/flash-attributes.html)
- [Special Bean Types](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-servlet/special-bean-types.html)