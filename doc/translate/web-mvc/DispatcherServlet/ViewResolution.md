# View Resolution

Spring MVC 定义了 ViewResolver 和 View 接口，使您能够在浏览器中呈现模型，而无需绑定到特定的视图技术。ViewResolver 提供了视图名称与实际视图之间的映射关系。View 负责在将数据交给特定视图技术之前进行准备工作。

下表提供了有关 ViewResolver 层次结构的更多详细信息：

|ViewResolver|	Description|
| --- | --- |
|AbstractCachingViewResolver|AbstractCachingViewResolver 的子类会缓存它们解析的视图实例。缓存可以提升某些视图技术的性能。您可以通过将 cache 属性设置为 false 来关闭缓存。此外，如果您必须在运行时刷新某个视图（例如，当 FreeMarker 模板被修改时），可以使用 removeFromCache(String viewName, Locale loc) 方法。|
|UrlBasedViewResolver|ViewResolver 接口的简单实现，它能够直接将逻辑视图名称解析为 URL，而无需显式的映射定义。如果你的逻辑名称与视图资源的名称以直接的方式匹配，无需进行任意的映射，那么这种方式是合适的。|
|InternalResourceViewResolver|UrlBasedViewResolver 的一个便捷子类，支持 InternalResourceView（实际上就是 Servlet 和 JSP）以及 JstlView 等子类。您可以通过 setViewClass(..) 方法为该解析器生成的所有视图指定视图类。详情请参阅 UrlBasedViewResolver 的 JavaDoc。|
|ContentNegotiatingViewResolver|实现 ViewResolver 接口，根据请求文件名或 Accept 头解析视图。参见内容协商。|
| BeanNameViewResolver|实现 `ViewResolver` 接口，该接口将视图名称解释为当前应用上下文中的 bean 名称。这是一种非常灵活的变体，允许根据不同的视图名称混合搭配使用不同的视图类型。每个这样的视图都可以被定义为一个 bean，例如在 XML 或配置类中定义。|


# Handling
你可以通过声明多个解析器 Bean 来串联视图解析器，并在必要时设置 `order` 属性以指定顺序。请注意，`order` 属性值越高，视图解析器在链中的位置越靠后。

视图解析器的契约规定，它可以返回 `null` 来表示无法找到视图。然而，对于 JSP 和 `InternalResourceViewResolver`，判断 JSP 是否存在的唯一方法是使用 `RequestDispatcher` 进行转发。因此，你必须在所有视图解析器的顺序中始终将 `InternalResourceViewResolver` 配置为最后一位。

配置视图解析非常简单，只需在 Spring 配置中添加 `ViewResolver` Bean 即可。MVC 配置为视图解析器和添加无逻辑视图控制器（用于在没有控制器逻辑的情况下进行 HTML 模板渲染）提供了专门的配置 API。

# 重定向(Redirecting)
特殊重定向：视图名称中的前缀`prefix`允许你执行重定向。`UrlBasedViewResolver`（及其子类）会将此识别为需要进行重定向的指令。视图名称的其余部分即为重定向的URL。

其最终效果与控制器返回`RedirectView`相同，但现在控制器本身可以基于逻辑视图名称进行操作。逻辑视图名称（例如`redirect:/myapp/some/resource`）会相对于当前的Servlet上下文进行重定向，而像`redirect:https://myhost.com/some/arbitrary/path`这样的名称则会重定向到一个绝对URL。

# Forwarding

你也可以为视图名称使用特殊的 `forward:` 前缀，这些视图名称最终会由 `UrlBasedViewResolver` 及其子类解析。这样会创建一个 `InternalResourceView`，它执行 `RequestDispatcher.forward()`。因此，这个前缀在 `InternalResourceViewResolver` 和 `InternalResourceView`（用于 JSP）中并不适用，但如果你使用其他视图技术，但仍希望强制将资源的转发交给 Servlet/JSP 引擎处理，这个前缀可能会有所帮助。请注意，你也可以选择链式配置多个视图解析器。


# 内容协商(Content Negotiation)
ContentnegotiatingViewResolver 本身并不解析视图，而是委托给其他视图解析器，并根据客户端请求的表示形式选择合适的视图。表示形式可以从 Accept 头信息或查询参数（例如 "/path?format=pdf"）中确定。

ContentnegotiatingViewResolver 通过将请求的媒体类型与每个 ViewResolver 关联的视图所支持的媒体类型（也称为 Content-Type）进行比较，从而选择合适的视图来处理请求。列表中第一个具有兼容 Content-Type 的视图会将表示形式返回给客户端。如果 ViewResolver 链无法提供兼容的视图，则会参考通过 DefaultViews 属性指定的视图列表。后一种选项适用于单例视图，这些视图可以渲染当前资源的适当表示形式，而无需考虑逻辑视图名称。Accept 头信息可以包含通配符（例如 text/*），在这种情况下，Content-Type 为 text/xml 的视图就是兼容的匹配项。

有关配置详情，请参阅 MVC 配置中的“视图解析器”部分。


