# Handler Methods

@RequestMapping 处理方法的签名非常灵活，可以选择多种受支持的控制器方法参数和返回值。

# 1.方法参数(Method Arguments)

下表描述了支持的控制器方法参数。任何参数均不支持响应式类型。
JDK 8 的 java.util.Optional 可作为方法参数，与具有 required 属性的注解（例如 @RequestParam、@RequestHeader 等）结合使用，等同于 required=false。

[table](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-methods/arguments.html)

|Controller method argument	| Description | example|
| --- | --- | --- |
| WebRequest, NativeWebRequest|对请求参数、请求属性和会话属性的通用访问，无需直接使用Servlet API.|
|jakarta.servlet.ServletRequest,jakarta.servlet.ServletResponse|选择任何特定的请求或响应类型——例如，ServletRequest、HttpServletRequest，或Spring的MultipartRequest、MultipartHttpServletRequest。|
|jakarta.servlet.http.HttpSession|强制要求存在一个会话。因此，此类参数永远不会为空。请注意，会话访问不是线程安全的。如果允许多个请求同时访问一个会话，请考虑将 `RequestMappingHandlerAdapter` 实例的 `synchronizeOnSession` 标志设置为 `true`。|id=F3B9A0FD4A5C5F511535741A05AD8164,createTime=1790134002184,lastAccessTime=1790134002184,maxInactiveInterval=1800,isNew=true,attributes={}|
|jakarta.servlet.http.PushBuilder|Servlet 4.0 提供了用于编程式 HTTP/2 资源推送的 PushBuilder API。请注意，根据 Servlet 规范，如果客户端不支持该 HTTP/2 功能，则注入的 PushBuilder 实例可以为 null。|
|java.security.Principal|当前已认证的用户——如果已知，可能是一个特定的 Principal 实现类。 请注意，此参数不会立即解析，如果它被注解了，以便在回退到通过 HttpServletRequest#getUserPrincipal 进行默认解析之前，允许自定义解析器对其进行解析。例如，Spring Security 的 Authentication 实现了 Principal，并且会通过 HttpServletRequest#getUserPrincipal 注入为该类型，除非它同时被注解了 @AuthenticationPrincipal，在这种情况下，它将通过 Spring Security 的自定义解析器通过 Authentication#getPrincipal 进行解析。|
|HttpMethod|The HTTP method of the request|GET|
|java.util.Locale|当前请求的区域设置，由可用的最具体的LocaleResolver（实际上是已配置的LocaleResolver或LocaleContextResolver）确定。|zh_CN|
|java.util.TimeZone + java.time.ZoneId|与当前请求相关联的时区，由LocaleContextResolver确定。|timeZone: sun.util.calendar.ZoneInfo[id="Asia/Shanghai",offset=28800000,dstSavings=0,useDaylight=false,transitions=31,lastRule=null],zoneId: Asia/Shanghai|
java.io.InputStream, java.io.Reader|用于访问 Servlet API 所暴露的原始请求体。|
|java.io.OutputStream, java.io.Writer|用于访问 Servlet API 所暴露的原始请求体|
|@PathVariable|用于访问 URI 模板变量。请参阅 URI 模式。|
|@MatrixVariable|用于访问URI路径段中的名称-值对。参见矩阵变量|
|@RequestParam|1. 用于访问Servlet请求参数，包括多部分文件。参数值会被转换为声明的方法参数类型。另见@RequestParam以及Multipart。2. 请注意，对于简单的参数值，使用@RequestParam是可选的。另见本表格末尾的“任何其他参数”。|
|@RequestHeader|用于访问请求头。头部的值会被转换为声明的方法参数类型。参见 @RequestHeader。|
|@CookieValue|用于访问Cookie。Cookie值将被转换为声明的方法参数类型。参见@CookieValue。|
|@RequestBody|用于访问HTTP请求体。请求体内容通过HttpMessageConverter实现转换为声明的方法参数类型。参见@RequestBody。|
|HttpEntity<B>|用于访问请求头和请求体。请求体通过 HttpMessageConverter 进行转换。参见 HttpEntity。|
|@RequestPart|要访问 multipart/form-data 请求中的某个部分，可以使用 HttpMessageConverter 将该部分的正文进行转换。请参阅 Multipart。|
|java.util.Map, org.springframework.ui.Model, org.springframework.ui.ModelMap|用于访问在 HTML 控制器中使用的模型，并将其作为视图渲染的一部分暴露给模板。|
|RedirectAttributes|指定在重定向情况下使用的属性（即附加到查询字符串中）以及临时存储的闪存属性，直到重定向后的一次请求为止。请参阅重定向属性和闪存属性。|
|@ModelAttribute|用于访问模型中已有的属性（如果不存在则实例化），并应用数据绑定和验证。请参见 @ModelAttribute 以及 Model 和 DataBinder。 请注意，使用 @ModelAttribute 是可选的（例如，用于设置其属性）。请参见本表末尾的“其他任何参数”。|
|Errors, BindingResult|要访问命令对象（即 `@ModelAttribute` 参数）的验证和数据绑定错误，或者 `@RequestBody` 或 `@RequestPart` 参数的验证错误，您必须在经过验证的方法参数之后立即声明一个 `Errors` 或 `BindingResult` 参数。|
|SessionStatus + class-level @SessionAttributes|用于标记表单处理已完成，从而触发清理通过类级 @SessionAttributes 注解声明的会话属性。更多详情请参见 @SessionAttributes。|
|UriComponentsBuilder|用于根据当前请求的主机、端口、协议、上下文路径以及Servlet映射的字面部分来构建一个URL。参见URI链接。|
|@SessionAttribute|要访问任何会话属性（与模型属性不同），这些属性是作为类级别的 @SessionAttributes 声明的结果存储在会话中的。更多详情请参见 @SessionAttribute。|
|@RequestAttribute|用于访问请求属性。更多详情请参见 @RequestAttribute。|
|Any other argument|如果一个方法参数未匹配到此表中的任何先前值，并且它是一个简单类型（通过 BeanUtils#isSimpleProperty 确定），则该参数将被解析为 @RequestParam。否则，它将被解析为 @ModelAttribute。|

# 2.返回值(Return Values)
下表描述了支持的控制器方法返回值。所有返回值均支持反应式类型。

# 3.Type Conversion
一些表示基于字符串的请求输入的注解控制器方法参数（如 @RequestParam、@RequestHeader、@PathVariable、@MatrixVariable 和 @CookieValue），如果声明为非 String 类型，可能需要类型转换。

在这种情况下，类型转换会根据已配置的转换器自动应用。默认情况下，支持简单类型（如 int、long、Date 等）。您可以通过 WebDataBinder（参见 DataBinder）或通过向 FormattingConversionService 注册 Formatter 来自定义类型转换。详见 Spring 字段格式化。

类型转换中的一个实际问题是空字符串源值的处理。如果类型转换后结果为 null，则该值被视为缺失。对于 Long、UUID 等目标类型可能会出现这种情况。如果您希望允许注入 null，可以在参数注解上使用 required 标志，或将参数声明为 @Nullable。

从 5.3 版本开始，即使在进行类型转换后，也必须强制执行非空参数。如果你的处理程序方法也打算接受空值，可以将参数声明为 `@Nullable`，或者在相应的 `@RequestParam` 等注解中将其标记为 `required=false`。这是一种最佳实践，也是解决在 5.3 升级中遇到的回归问题的推荐方案。

另外，你也可以专门处理某些情况，例如在 `@PathVariable` 为必需参数时，处理由此产生的 `MissingPathVariableException`。转换后的空值将被视为原始值为空，因此会抛出相应的 `Missing…Exception` 变体。

