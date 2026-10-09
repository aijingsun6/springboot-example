# Servlet Web Applications

如果你想构建基于Servlet的Web应用程序，可以利用Spring Boot对Spring MVC或Jersey的自动配置功能。

Spring Web MVC 框架（通常称为“Spring MVC”）是一个功能丰富的“模型-视图-控制器”（MVC）Web 框架。Spring MVC 允许你创建特殊的 `@Controller` 或 `@RestController` Bean 来处理传入的 HTTP 请求。控制器中的方法通过使用 `@RequestMapping` 注解映射到 HTTP。

以下代码展示了一个典型的用于提供 JSON 数据的 `@RestController`：

```bash

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class MyRestController {

	private final UserRepository userRepository;

	private final CustomerRepository customerRepository;

	public MyRestController(UserRepository userRepository, CustomerRepository customerRepository) {
		this.userRepository = userRepository;
		this.customerRepository = customerRepository;
	}

	@GetMapping("/{userId}")
	public User getUser(@PathVariable Long userId) {
		return this.userRepository.findById(userId).get();
	}

	@GetMapping("/{userId}/customers")
	public List<Customer> getUserCustomers(@PathVariable Long userId) {
		return this.userRepository.findById(userId).map(this.customerRepository::findByUser).get();
	}

	@DeleteMapping("/{userId}")
	public void deleteUser(@PathVariable Long userId) {
		this.userRepository.deleteById(userId);
	}

}
```

"WebMvc.fn" 是函数式变体，它将路由配置与实际请求处理分离开来，如下例所示：

```bash

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.accept;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
public class MyRoutingConfiguration {

	private static final RequestPredicate ACCEPT_JSON = accept(MediaType.APPLICATION_JSON);

	@Bean
	public RouterFunction<ServerResponse> routerFunction(MyUserHandler userHandler) {
		return route()
				.GET("/{user}", ACCEPT_JSON, userHandler::getUser)
				.GET("/{user}/customers", ACCEPT_JSON, userHandler::getUserCustomers)
				.DELETE("/{user}", ACCEPT_JSON, userHandler::deleteUser)
				.build();
	}

}
```
```bash

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class MyUserHandler {

	public ServerResponse getUser(ServerRequest request) {
		...
	}

	public ServerResponse getUserCustomers(ServerRequest request) {
		...
	}

	public ServerResponse deleteUser(ServerRequest request) {
		...
	}

}
```

Spring MVC 是 Spring 框架的核心组成部分，详细信息可在参考文档中查阅。此外，spring.io/guides 网站也提供了多份关于 Spring MVC 的[指南](https://spring.io/guides)。


你可以根据需要定义任意数量的 RouterFunction bean，以模块化路由的定义。如果需要应用优先级，可以对这些 bean 进行排序。


# Spring MVC Auto-configuration
Spring Boot 为 Spring MVC 提供了自动配置，该配置适用于大多数应用程序。它取代了 @EnableWebMvc 的使用，并且两者不能同时使用。除了 Spring MVC 的默认配置外，自动配置还提供了以下特性：

- 包含 `ContentnegotiatingViewResolver` 和 `BeanNameViewResolver` Bean。
- 支持提供静态资源，包括对 WebJars 的支持（将在本文档后续部分介绍）。
- 自动注册 `Converter`、`GenericConverter` 和 `Formatter` Bean。
- 支持 `HttpMessageConverters`（将在本文档后续部分介绍）。
- 自动注册 `MessageCodesResolver`（将在本文档后续部分介绍）。
- 支持静态 `index.html`。
- 自动使用 `ConfigurableWebBindingInitializer` Bean（将在本文档后续部分介绍）。

如果您希望保留 Spring Boot MVC 的这些自定义配置，并且希望进行更多的 MVC 自定义（如拦截器、格式化器、视图控制器等），您可以添加自己的 `@Configuration` 类，该类类型为 `WebMvcConfigurer`，但不使用 `@EnableWebMvc`。

如果您希望提供 `RequestMappingHandlerMapping`、`RequestMappingHandlerAdapter` 或 `ExceptionHandlerExceptionResolver` 的自定义实例，同时仍保留 Spring Boot MVC 的自定义配置，您可以声明一个类型为 `WebMvcRegistrations` 的 Bean，并使用它来提供这些组件的自定义实例。这些自定义实例将接受 Spring MVC 的进一步初始化和配置。如果您希望参与并覆盖后续的处理过程，可以使用 `WebMvcConfigurer`。

如果您不希望使用自动配置，并且希望完全控制 Spring MVC，可以添加自己的 `@Configuration` 类，并使用 `@EnableWebMvc` 注解。或者，您也可以添加自己的 `@Configuration` 注解的 `DelegatingWebMvcConfiguration` 类，具体方法请参考 `@EnableWebMvc` API 文档。

# Spring MVC Conversion Service

Spring MVC 使用的 `ConversionService` 与用于转换 `application.properties` 或 `application.yaml` 文件中值的 `ConversionService` 不同。这意味着 `Period`、`Duration` 和 `DataSize` 转换器不可用，并且 `@DurationUnit` 和 `@DataSizeUnit` 注解将被忽略。

如果您想自定义 Spring MVC 使用的 `ConversionService`，可以提供一个带有 `addFormatters` 方法的 `WebMvcConfigurer` bean。通过此方法，您可以注册任何您喜欢的转换器，或者可以委托给 `ApplicationConversionService` 上可用的静态方法。

您也可以使用 `spring.mvc.format.*` 配置属性来定制转换。如果未进行配置，将使用以下默认值：

| Property |DateTimeFormatter|	Formats |
| --- | --- | --- |
|spring.mvc.format.date|ofLocalizedDate(FormatStyle.SHORT)|java.util.Date and LocalDate
| spring.mvc.format.time|ofLocalizedTime(FormatStyle.SHORT)|java.time’s LocalTime and OffsetTime
| spring.mvc.format.date-time|ofLocalizedDateTime(FormatStyle.SHORT)|java.time’s LocalDateTime, OffsetDateTime, and ZonedDateTime

# HttpMessageConverters

Spring MVC 使用 `HttpMessageConverter` 接口来转换 HTTP 请求和响应。它默认提供了合理的默认值。例如，对象可以自动转换为 JSON（通过使用 Jackson 库）或 XML（如果可用，通过使用 Jackson XML 扩展；如果不可用，则通过使用 JAXB）。默认情况下，字符串会以 UTF-8 编码。

上下文中存在的任何 `HttpMessageConverter` Bean 都会被添加到转换器列表中。你也可以以同样的方式覆盖默认的转换器。

如果你需要添加或自定义转换器，可以声明一个或多个 `ClientHttpMessageConvertersCustomizer` 或 `ServerHttpMessageConvertersCustomizer` 作为 Bean。在那里，你可以选择是否在默认转换器之前添加转换器实例（使用 `addCustomConverter`），或者是否覆盖特定的默认转换器（例如使用 `likeJsonConverter`）。

以下示例清单展示了具体用法：

```bash

import java.text.SimpleDateFormat;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.boot.http.converter.autoconfigure.ServerHttpMessageConvertersCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverters.ClientBuilder;
import org.springframework.http.converter.HttpMessageConverters.ServerBuilder;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

@Configuration(proxyBeanMethods = false)
public class MyHttpMessageConvertersConfiguration {

	@Bean
	public ClientHttpMessageConvertersCustomizer myClientConvertersCustomizer() {
		return (clientBuilder) -> clientBuilder.addCustomConverter(new AdditionalHttpMessageConverter())
			.addCustomConverter(new AnotherHttpMessageConverter());
	}

	@Bean
	public JacksonConverterCustomizer jacksonConverterCustomizer() {
		JsonMapper jsonMapper = JsonMapper.builder().defaultDateFormat(new SimpleDateFormat("yyyy-MM")).build();
		return new JacksonConverterCustomizer(jsonMapper);
	}

	// contribute a custom JSON converter to both client and server
	static class JacksonConverterCustomizer
			implements ClientHttpMessageConvertersCustomizer, ServerHttpMessageConvertersCustomizer {

		private final JsonMapper jsonMapper;

		JacksonConverterCustomizer(JsonMapper jsonMapper) {
			this.jsonMapper = jsonMapper;
		}

		@Override
		public void customize(ClientBuilder builder) {
			builder.withJsonConverter(new JacksonJsonHttpMessageConverter(this.jsonMapper));
		}

		@Override
		public void customize(ServerBuilder builder) {
			builder.withJsonConverter(new JacksonJsonHttpMessageConverter(this.jsonMapper));
		}

	}

}
```
# 消息代码解析器(MessageCodesResolver)

Spring MVC 有一个策略，用于从绑定错误生成错误代码以渲染错误信息：MessageCodesResolver。如果你设置了 `spring.mvc.message-codes-resolver-format` 属性为 `PREFIX_ERROR_CODE` 或 `POSTFIX_ERROR_CODE`，Spring Boot 会为你自动创建一个（参见 `DefaultMessageCodesResolver.Format` 中的枚举）。

# 静态内容(Static Content)

默认情况下，Spring Boot从类路径中名为/static的目录（或/public或/resources或/META-INF/resources）或ServletContext的根目录提供静态内容。它使用了Spring MVC中的ResourceHttpRequestHandler，因此您可以通过添加自己的WebMvcConfigurer并重写addResourceHandlers方法来修改该行为。

在独立Web应用程序中，不会启用容器中的默认servlet。可以使用server.servlet.register-default-servlet属性启用它。

默认的Servlet充当回退，如果Spring决定不处理它，则从ServletContext的根目录提供内容。大多数情况下，这种情况不会发生（除非你修改了默认的MVC配置），因为Spring总是可以通过DispatcherServlet来处理请求。

默认情况下，资源映射在/**上，但您可以使用spring.mvc.static-path-pattern属性来调整它。例如，将所有资源重新定位到/resources/**可以按如下方式实现：

```bash

spring.mvc.static-path-pattern=/resources/**
```

您还可以使用spring.web.resources.static-locations属性（使用目录位置列表替换默认值）自定义静态资源位置。根servlet上下文路径“/”也会自动添加为位置。

除了前面提到的“标准”静态资源位置之外，还有一个特殊情况是Webjars内容。默认情况下，路径在/webjars/**中的任何资源都是从jar文件中提供的，如果它们是以Webjars格式打包的。可以使用spring.mvc.webjars-path-pattern属性自定义路径。


如果你的应用程序打包为 jar 文件，请不要使用 src/main/webapp 目录。虽然该目录是一个常见的标准，但它仅适用于 war 打包方式，如果你生成的是 jar 文件，大多数构建工具会默默地忽略它。

Spring Boot 还支持 Spring MVC 提供的高级资源处理功能，允许使用诸如缓存破坏静态资源或为 Webjars 使用版本无关 URL 的用例。 
要为 Webjars 使用版本无关 URL，需添加 `org.webjars:webjars-locator-lite` 依赖。然后声明您的 Webjar。以 jQuery 为例，添加 `/webjars/jquery/jquery.min.js` 会生成 `/webjars/jquery/x.y.z/jquery.min.js`，其中 x.y.z 是 Webjar 的版本号。 
要使用缓存破坏功能，以下配置可为所有静态资源设置缓存破坏解决方案，从而在 URL 中有效添加内容哈希值，例如 `<link href="/css/spring-2a2d595e6ed9a0b24f027f2b63b134d6.css"/>`：

```bash

spring.web.resources.chain.strategy.content.enabled=true
spring.web.resources.chain.strategy.content.paths=/**
```

资源链接在运行时通过模板进行重写，这要归功于为 Thymeleaf 和 FreeMarker 自动配置的 `ResourceUrlEncodingFilter`。在使用 JSP 时，您需要手动声明此过滤器。目前其他模板引擎尚未自动支持，但可以通过自定义模板宏/助手以及使用 `ResourceUrlProvider` 来实现。

当使用例如 JavaScript 模块加载器动态加载资源时，重命名文件并不是一个可行的选择。因此，还支持其他策略，并且可以组合使用。"固定"策略会在 URL 中添加静态版本字符串，而不会更改文件名，如下例所示：


```bash

spring.web.resources.chain.strategy.content.enabled=true
spring.web.resources.chain.strategy.content.paths=/**
spring.web.resources.chain.strategy.fixed.enabled=true
spring.web.resources.chain.strategy.fixed.paths=/js/lib/
spring.web.resources.chain.strategy.fixed.version=v12
```

通过此配置，位于“/js/lib/”目录下的JavaScript模块采用固定的版本策略（如“/v12/js/lib/mymodule.js”），而其他资源仍使用内容版本策略（如<link href="/css/spring-2a2d595e6ed9a0b24f027f2b63b134d6.css"/>）。 
更多支持的选项请参见WebProperties.Resources。 
该功能已在专门的博客文章和Spring Framework的参考文档中进行了详细描述。


# 欢迎页面(Welcome Page)

Spring Boot 支持静态和模板化的欢迎页面。它首先在配置的静态内容位置中查找 index.html 文件。如果未找到该文件，则会继续查找 index 模板。如果找到其中任何一个，它将自动用作应用程序的欢迎页面。

这仅作为应用程序定义的实际索引路由的备用方案。其顺序由 HandlerMapping bean 的顺序决定，默认为以下顺序：
| | |
|--- | --- |
| RouterFunctionMapping |Endpoints declared with RouterFunction beans
| RequestMappingHandlerMapping |Endpoints declared in @Controller beans
| WelcomePageHandlerMapping|The welcome page support

# Custom Favicon

与其他静态资源一样，Spring Boot 会在配置的静态内容位置中检查是否存在 favicon.ico 文件。如果存在该文件，它将自动用作应用程序的 favicon。

# Path Matching and Content Negotiation

Spring MVC 可以通过查看请求路径并将其与应用程序中定义的映射（例如，Controller 方法上的 @GetMapping 注解）进行匹配，从而将传入的 HTTP 请求映射到处理程序。Spring Boot 默认选择禁用后缀模式匹配，这意味着像 "GET /projects/spring-boot.json" 这样的请求将不会匹配到 @GetMapping("/projects/spring-boot") 的映射。这被认为是 Spring MVC 应用程序的最佳实践。此功能在过去对于那些未发送正确 "Accept" 请求头的 HTTP 客户端非常有用；我们需要确保向客户端发送正确的 Content Type。如今，内容协商已经变得更加可靠。

处理那些未始终发送正确 "Accept" 请求头的 HTTP 客户端还有其他方法。我们可以不使用后缀匹配，而是使用查询参数来确保像 "GET /projects/spring-boot?format=json" 这样的请求能够映射到 @GetMapping("/projects/spring-boot")：

```bash
spring.mvc.contentnegotiation.favor-parameter=true
```

或者，如果您想使用不同的参数名称

```bash

spring.mvc.contentnegotiation.favor-parameter=true
spring.mvc.contentnegotiation.parameter-name=myparam
```
大多数标准媒体类型开箱即用，但您也可以定义新的类型：

```bash

spring.mvc.contentnegotiation.media-types.markdown=text/markdown
```


从 Spring Framework 5.3 开始，Spring MVC 支持两种将请求路径与控制器匹配的策略。默认情况下，Spring Boot 使用 PathPatternParser 策略。PathPatternParser 是一种经过优化的实现，但与 AntPathMatcher 策略相比，它存在一些限制。PathPatternParser 限制了某些路径模式变体的使用。此外，它也不兼容使用路径前缀（spring.mvc.servlet.path）配置 DispatcherServlet 的方式。

可以通过 `spring.mvc.pathmatch.matching-strategy` 配置属性来设置该策略，如下例所示：

```bash

spring.mvc.pathmatch.matching-strategy=ant-path-matcher
```

如果请求没有找到对应的处理器，Spring MVC 将抛出 NoHandlerFoundException。请注意，默认情况下，静态内容的访问路径被映射为 /**，因此会为所有请求提供处理器。如果没有可用的静态内容，ResourceHttpRequestHandler 将抛出 NoResourceFoundException。要使 NoHandlerFoundException 被抛出，请将 spring.mvc.static-path-pattern 设置为更具体的值，例如 /resources/**，或将 spring.web.resources.add-mappings 设置为 false 以完全禁用静态内容的提供。


# ConfigurableWebBindingInitializer

Spring MVC 使用 `WebBindingInitializer` 来为特定请求初始化 `WebDataBinder`。如果你创建了自己的 `ConfigurableWebBindingInitializer` @Bean，Spring Boot 会自动配置 Spring MVC 使用它。

# Template Engines

除了REST Web服务，您还可以使用Spring MVC来提供动态HTML内容。Spring MVC支持多种模板技术，包括Thymeleaf、FreeMarker和JSP。此外，许多其他模板引擎也包含它们自己的Spring MVC集成。

Spring Boot为以下模板引擎提供了自动配置支持：

- FreeMarker
- Groovy
- Thymeleaf
- Mustache

如果可能，应避免使用JSP。在使用嵌入式Servlet容器时，使用JSP存在一些已知的限制。

当您使用这些模板引擎之一并采用默认配置时，模板会自动从src/main/resources/templates目录中加载。

根据您运行应用程序的方式，IDE可能会以不同的顺序排列类路径。从IDE的主方法运行应用程序时，其顺序与使用Maven或Gradle运行应用程序或从打包的jar文件运行应用程序时的顺序不同。这可能导致Spring Boot无法找到预期的模板。如果您遇到此问题，可以在IDE中重新排列类路径，将模块的类和资源放在首位。


# Error Handling
默认情况下，Spring Boot 提供了一个 `/error` 映射，以合理的方式处理所有错误，并且它被注册为 Servlet 容器中的“全局”错误页面。对于机器客户端，它会生成一个包含错误详情、HTTP 状态码和异常信息的 JSON 响应。对于浏览器客户端，则会使用“白标”错误视图，以 HTML 格式呈现相同的数据（要自定义该视图，可以添加一个解析为 `error` 的视图）。

如果您想自定义默认的错误处理行为，可以设置一些 `spring.web.error` 属性。请参阅附录中的 Web 属性部分。

要完全替换默认行为，可以实现 `ErrorController` 接口并注册该类型的 Bean 定义，或者添加一个 `ErrorAttributes` 类型的 Bean，以使用现有机制但替换其内容。

`BasicErrorController` 可以用作自定义 `ErrorController` 的基类。如果您想为新的内容类型添加处理器（默认情况下仅处理 `text/html` 并提供其他所有内容的回退），这将特别有用。为此，可以扩展 `BasicErrorController`，添加一个带有 `@RequestMapping` 注解的公共方法（该注解具有 `produces` 属性），并创建一个您新类型的 Bean。

从 Spring Framework 6.0 开始，支持 RFC 9457 的问题详情（Problem Details）。Spring MVC 可以生成具有 `application/problem+json` 媒体类型的自定义错误信息，例如：

```bash

{
	"type": "https://example.org/problems/unknown-project",
	"title": "Unknown project",
	"status": 404,
	"detail": "No project found for id 'spring-unknown'",
	"instance": "/projects/spring-unknown"
}
```
可以通过将 `spring.mvc.problemdetails.enabled` 设置为 `true` 来启用此支持。 
您还可以定义一个带有 `@ControllerAdvice` 注解的类，以自定义针对特定控制器和/或异常类型返回的 JSON 文档，如下例所示：

```bash

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(basePackageClasses = SomeController.class)
public class MyControllerAdvice extends ResponseEntityExceptionHandler {

	@ResponseBody
	@ExceptionHandler(MyException.class)
	public ResponseEntity<?> handleControllerException(HttpServletRequest request, Throwable ex) {
		HttpStatus status = getStatus(request);
		return new ResponseEntity<>(new MyErrorBody(status.value(), ex.getMessage()), status);
	}

	private HttpStatus getStatus(HttpServletRequest request) {
		Integer code = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		HttpStatus status = HttpStatus.resolve(code);
		return (status != null) ? status : HttpStatus.INTERNAL_SERVER_ERROR;
	}

}
```
在前面的示例中，如果 `MyException` 是由与 `SomeController` 定义在同一个包中的控制器抛出的，则将使用 `MyErrorBody` POJO 的 JSON 表示形式，而不是 `ErrorAttributes` 的表示形式。在某些情况下，控制器级别处理的错误不会被 Web 观察或指标基础设施记录。应用程序可以通过在观察上下文中设置已处理的异常，来确保此类异常被记录在观察中。

# Custom Error Pages

如果您想为特定状态码显示自定义的 HTML 错误页面，可以将文件添加到 `/error` 目录中。错误页面可以是静态 HTML（即添加到任何静态资源目录中），也可以通过模板构建。文件名应为确切的状态码或系列掩码。

例如，要将 404 映射到静态 HTML 文件，您的目录结构应如下所示：

```bash

src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- public/
             +- error/
             |   +- 404.html
             +- <other public assets>
```
要使用 FreeMarker 模板映射所有 5xx 错误，您的目录结构应如下所示：

```bash

src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- templates/
             +- error/
             |   +- 5xx.ftlh
             +- <other templates>
```

对于更复杂的映射，您还可以添加实现 ErrorViewResolver 接口的 bean，如下例所示：

```bash

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

public class MyErrorViewResolver implements ErrorViewResolver {

	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
		// Use the request or status to optionally return a ModelAndView
		if (status == HttpStatus.INSUFFICIENT_STORAGE) {
			// We could add custom model values here
			return new ModelAndView("myview");
		}
		return null;
	}

}
```

你也可以使用常规的 Spring MVC 功能，例如 `@ExceptionHandler` 方法和 `@ControllerAdvice`。然后，`ErrorController` 会捕获所有未处理的异常。


# Mapping Error Pages Outside of Spring MVC

对于不使用 Spring MVC 的应用程序，您可以直接使用 ErrorPageRegistrar 接口来注册 ErrorPage 实例。这种抽象直接与底层的嵌入式 Servlet 容器配合工作，即使您没有 Spring MVC 的 DispatcherServlet，它也能正常运行。

```bash

import org.springframework.boot.web.error.ErrorPage;
import org.springframework.boot.web.error.ErrorPageRegistrar;
import org.springframework.boot.web.error.ErrorPageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration(proxyBeanMethods = false)
public class MyErrorPagesConfiguration {

	@Bean
	public ErrorPageRegistrar errorPageRegistrar() {
		return this::registerErrorPages;
	}

	private void registerErrorPages(ErrorPageRegistry registry) {
		registry.addErrorPages(new ErrorPage(HttpStatus.BAD_REQUEST, "/400"));
	}

}
```


如果您注册了一个以路径结尾的 ErrorPage，而该路径最终由过滤器（Filter）处理（这在非 Spring 的一些 Web 框架中很常见，例如 Jersey 和 Wicket），那么必须显式地将该过滤器注册为 ERROR 分派器，如下例所示：

```bash

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MyFilterConfiguration {

	@Bean
	public FilterRegistrationBean<MyFilter> myFilter() {
		FilterRegistrationBean<MyFilter> registration = new FilterRegistrationBean<>(new MyFilter());
		// ...
		registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
		return registration;
	}

}
```

请注意，默认的FilterRegistrationBean不包含ERROR分发器类型。


# CORS Support

跨域资源共享（CORS）是一项由大多数浏览器实现的W3C规范，它允许您以灵活的方式指定哪些类型的跨域请求被授权，而不是使用某些安全性较低且功能较弱的方法（如IFRAME或JSONP）。

自4.2版本起，Spring MVC支持CORS。在Spring Boot应用程序中使用@CrossOrigin注解进行控制器方法的CORS配置时，无需任何特殊配置。全局CORS配置可以通过注册一个带有自定义addCorsMappings(CorsRegistry)方法的WebMvcConfigurer Bean来定义，如下所示：

```bash

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class MyCorsConfiguration {

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {

			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**");
			}

		};
	}

}
```

# API Versioning

Spring MVC 支持 API 版本控制，可用于随时间推移演进 HTTP API。同一个 `@Controller` 路径可以多次映射，以支持不同版本的 API。 
更多详情请参阅 Spring Framework 的参考文档。 
添加映射后，您还需要配置 Spring MVC，使其能够使用请求中附带的任何版本信息。通常，版本信息会作为 HTTP 头、查询参数、媒体类型参数或路径的一部分发送。 
要配置 Spring MVC，您可以使用 `WebMvcConfigurer` Bean 并重写 `configureApiVersioning（…）` 方法，或者使用属性配置。 
例如，以下配置将使用 `X-Version` HTTP 头来获取版本信息，并在未发送该头时默认使用 1.0.0。


```bash

spring.mvc.apiversion.default=1.0.0
spring.mvc.apiversion.use.header=X-Version
```

如果你的设置需要多种策略（例如头部信息和查询参数），请考虑通过重写 `configureApiVersioning` 方法以编程方式声明顺序。 
为了获得更完整的控制，你还可以定义 `ApiVersionResolver`、`ApiVersionParser` 和 `ApiVersionDeprecationHandler` Bean，这些 Bean 将被注入到自动配置的 Spring MVC 配置中。 
API 版本控制同样支持 WebClient 和 RestClient。详情请参见 API 版本控制部分。

# Embedded Servlet Container Support

对于 Servlet 应用程序，Spring Boot 包含了对嵌入式 Tomcat 和 Jetty 服务器的支持。大多数开发者会使用相应的启动器来获取一个完全配置好的实例。默认情况下，嵌入式服务器会在 8080 端口监听 HTTP 请求。

## Servlets, Filters, and Listeners
在使用嵌入式Servlet容器时，您可以通过Spring Bean或扫描Servlet组件的方式，注册Servlet、过滤器以及Servlet规范中的所有监听器（例如HttpSessionListener）。

## Registering Servlets, Filters, and Listeners as Spring Beans

任何作为 Spring Bean 的 Servlet、Filter 或 servlet *Listener 实例都会注册到嵌入式容器中。如果您希望在配置过程中引用 application.properties 中的值，这将特别方便。

默认情况下，如果上下文中仅包含一个 Servlet，则该 Servlet 会被映射到 `/`。如果存在多个 servlet Bean，则使用 Bean 名称作为路径前缀。Filter 则映射到 `/*`。

如果基于约定的映射不够灵活，您可以使用 `ServletRegistrationBean`、`FilterRegistrationBean` 和 `ServletListenerRegistrationBean` 类来完全控制。如果您更喜欢使用注解而不是 `ServletRegistrationBean` 和 `FilterRegistrationBean`，也可以使用 `@ServletRegistration` 和 `@FilterRegistration` 作为替代方案。

通常，让 Filter Bean 保持无序是安全的。如果需要特定的顺序，您应该使用 `@Order` 注解 Filter 或者让 Filter 实现 `Ordered` 接口。您不能通过在 Filter 的 Bean 方法上使用 `@Order` 注解来配置其顺序。如果您无法更改 Filter 类以添加 `@Order` 或实现 `Ordered`，您必须为该 Filter 定义一个 `FilterRegistrationBean`，并使用 `setOrder(int)` 方法设置注册 Bean 的顺序。或者，如果您更喜欢使用注解，也可以使用 `@FilterRegistration` 并设置 `order` 属性。请避免将读取请求体的 Filter 配置为 `Ordered.HIGHEST_PRECEDENCE`，因为这可能会与应用程序的字符编码配置产生冲突。如果 Servlet Filter 包装了请求，则应将其配置为小于或等于 `Ordered.REQUEST_WRAPPER_FILTER_MAX_ORDER` 的顺序。


要查看应用程序中每个Filter的顺序，请启用Web日志组的调试级别日志记录（logging.level.web=debug）。启动时将记录已注册过滤器的详细信息，包括其顺序和URL模式。

在注册Filter bean时要小心，因为它们在应用程序生命周期中非常早地被初始化。如果需要注册一个与其他bean交互的Filter，请考虑使用DelegatingFilterProxyRegistrationBean。

## Servlet Context Initialization

嵌入式 Servlet 容器不会直接执行 ServletContainerInitializer 接口或 Spring 的 WebApplicationInitializer 接口。这是一个有意为之的设计决策，旨在降低第三方库（设计为在 WAR 包内运行）破坏 Spring Boot 应用程序的风险。

如果你需要在 Spring Boot 应用程序中执行 Servlet 上下文初始化，应该注册一个实现了 ServletContextInitializer 接口的 Bean。该接口的单一 onStartup 方法可以访问 ServletContext，并且如果需要，可以轻松用作现有 WebApplicationInitializer 的适配器。

## Init Parameters
可以在 ServletContext 上使用 `server.servlet.context-parameters.*` 属性来配置初始化参数。例如，属性 `server.servlet.context-parameters.com.example.parameter=example` 将配置一个名为 `com.example.parameter` 的 ServletContext 初始化参数，其值为 `example`。

## Scanning for Servlets, Filters, and listeners

在使用嵌入式容器时，可以通过使用 `@ServletComponentScan` 来启用对标注了 `@WebServlet`、`@WebFilter` 和 `@WebListener` 的类的自动注册。 
在独立容器中，`@ServletComponentScan` 不起作用，此时会使用容器内置的发现机制。

## The ServletWebServerApplicationContext
在底层，Spring Boot 使用一种不同类型的 ApplicationContext 来支持嵌入式 Servlet 容器。`ServletWebServerApplicationContext` 是一种特殊的 `WebApplicationContext`，它通过搜索单个 `ServletWebServerFactory` bean 来自行引导。通常，`TomcatServletWebServerFactory` 或 `JettyServletWebServerFactory` 会被自动配置。

你通常不需要了解这些实现类。大多数应用程序都是自动配置的，Spring Boot 会为你创建合适的 `ApplicationContext` 和 `ServletWebServerFactory`。

在嵌入式容器设置中，`ServletContext` 是作为服务器启动的一部分设置的，而服务器启动是在应用程序上下文初始化期间发生的。因此，`ApplicationContext` 中的 bean 不能可靠地使用 `ServletContext` 进行初始化。一种解决方法是将 `ApplicationContext` 注入为 bean 的依赖项，仅在需要时访问 `ServletContext`。另一种方法是使用回调函数，一旦服务器启动后执行。这可以通过 `ApplicationListener` 来实现，监听 `ApplicationStartedEvent`，如下所示：

```bash

import jakarta.servlet.ServletContext;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.WebApplicationContext;

public class MyDemoBean implements ApplicationListener<ApplicationStartedEvent> {

	private ServletContext servletContext;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		ApplicationContext applicationContext = event.getApplicationContext();
		this.servletContext = ((WebApplicationContext) applicationContext).getServletContext();
	}

}
```

## Customizing Embedded Servlet Containers

常见的 Servlet 容器设置可以通过 Spring Environment 属性进行配置。通常，您会在 `application.properties` 或 `application.yaml` 文件中定义这些属性。常见的服务器设置包括：

网络设置：用于接收 HTTP 请求的监听端口（`server.port`）、要绑定的接口地址（`server.address`）等。
会话设置：会话是否持久化（`server.servlet.session.persistent`）、会话超时时间（`server.servlet.session.timeout`）、会话数据存储位置（`server.servlet.session.store-dir`）以及会话 Cookie 配置（`server.servlet.session.cookie.*`）。
错误管理：错误页面的位置（`spring.web.error.path`）等。

Spring Boot 会尽可能多地暴露常见设置，但并非所有设置都能做到这一点。对于这些情况，专门的命名空间提供了针对特定服务器的自定义配置（参见 `server.tomcat`）。例如，可以借助嵌入式 Servlet 容器的特定功能来配置访问日志。


## SameSite Cookie
SameSite Cookie 属性可以被 Web 浏览器用来控制是否以及如何在跨站请求中提交 Cookie。该属性对于现代 Web 浏览器尤为重要，因为它们已经开始改变在缺少该属性时所使用的默认值。

如果您想更改会话 Cookie 的 SameSite 属性，可以使用 `server.servlet.session.cookie.same-site` 属性。该属性由自动配置的 Tomcat 和 Jetty 服务器支持。它也用于配置基于 Spring Session servlet 的 `SessionRepository` bean。

例如，如果您希望会话 Cookie 的 SameSite 属性为 None，可以在 `application.properties` 或 `application.yaml` 文件中添加以下内容：
```bash

server.servlet.session.cookie.same-site=none
```
如果您想更改添加到 `HttpServletResponse` 中的其他 Cookie 的 SameSite 属性，可以使用 `CookieSameSiteSupplier`。`CookieSameSiteSupplier` 会接收一个 Cookie，并可能返回一个 SameSite 值，或者返回 null。 
您可以使用一些方便的工厂方法和过滤器方法来快速匹配特定的 Cookie。例如，添加以下 Bean 将自动为所有名称与正则表达式 `myapp.*` 匹配的 Cookie 应用 Lax 的 SameSite 值。

```bash

import org.springframework.boot.web.server.servlet.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MySameSiteConfiguration {

	@Bean
	public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
		return CookieSameSiteSupplier.ofLax().whenHasNameMatching("myapp.*");
	}

}
```

## Character Encoding

嵌入式 Servlet 容器在处理请求和响应时的字符编码行为可以通过 `server.servlet.encoding.*` 配置属性进行设置。 
当请求的 `Accept-Language` 头字段指示了请求的地区设置（locale）时，Servlet 容器会自动将其映射为字符集。每个容器都提供了默认的地区设置到字符集的映射关系，您应确认这些映射是否满足应用程序的需求。如果不满足，可以使用 `server.servlet.encoding.mapping` 配置属性来自定义这些映射，如下例所示：

```bash

server.servlet.encoding.mapping.ko=UTF-8
```

在前面的示例中，ko（韩语）区域已被映射到UTF-8。这相当于传统war部署中web.xml文件中的<locale-encoding-mapping-list>条目。

## Programmatic Customization

如果您需要通过编程方式配置嵌入式 Servlet 容器，可以注册一个实现了 `WebServerFactoryCustomizer` 接口的 Spring Bean。`WebServerFactoryCustomizer` 提供了对 `ConfigurableServletWebServerFactory` 的访问，该工厂类包含许多自定义设置方法。以下示例展示了如何通过编程方式设置端口：
```bash

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

	@Override
	public void customize(ConfigurableServletWebServerFactory server) {
		server.setPort(9000);
	}

}
```



TomcatServletWebServerFactory 和 JettyServletWebServerFactory 是 ConfigurableServletWebServerFactory 的专用变体，它们分别提供了针对 Tomcat 和 Jetty 的额外自定义设置方法。以下示例展示了如何自定义 TomcatServletWebServerFactory，以访问 Tomcat 特定的配置选项：


```bash


import java.time.Duration;

import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class MyTomcatWebServerFactoryCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

	@Override
	public void customize(TomcatServletWebServerFactory server) {
		server.addConnectorCustomizers((connector) -> connector.setAsyncTimeout(Duration.ofSeconds(20).toMillis()));
	}

}
```

## Customizing ConfigurableServletWebServerFactory Directly

对于需要从 `ServletWebServerFactory` 扩展的更高级用例，您可以自己暴露此类类型的 Bean。
许多配置选项都提供了 setter 方法。如果您需要执行一些更特殊的操作，还提供了几个受保护的“钩子”方法。详情请参见 `ConfigurableServletWebServerFactory` 的 API 文档。
自动配置的定制器仍然会应用于您的自定义工厂，因此请谨慎使用该选项。

JSP 的限制
当运行使用嵌入式 Servlet 容器（并打包为可执行归档文件）的 Spring Boot 应用程序时，JSP 支持存在一些限制。
如果使用 WAR 打包，Jetty 和 Tomcat 应该可以正常工作。使用 `java -jar` 启动的可执行 WAR 文件可以正常运行，并且也可以部署到任何标准容器中。但是，在使用可执行 JAR 文件时，JSP 不受支持。
创建自定义的 `error.jsp` 页面不会覆盖默认的错误处理视图。应改用自定义错误页面。
如果您使用 `mvn spring-boot:run` 或 `gradle bootRun` 运行应用程序，并且偏离了标准的 `src/main/webapp` 目录结构，您可能需要设置 `WAR_SOURCE_DIRECTORY` 环境变量，以便 Spring Boot 能够找到您的 JSP 文件。