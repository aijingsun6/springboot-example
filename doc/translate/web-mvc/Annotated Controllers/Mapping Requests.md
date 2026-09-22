# Mapping Requests

# 1 RequestMapping
你可以使用 `@RequestMapping` 注解将请求映射到控制器方法。它具有多种属性，可以通过 URL、HTTP 方法、请求参数、请求头和媒体类型进行匹配。你可以在类级别使用它来表示共享的映射，或在方法级别使用它来缩小到特定的端点映射。

此外，还有针对特定 HTTP 方法的 `@RequestMapping` 快捷方式变体：
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
@PatchMapping


这些快捷方式是自定义注解，之所以提供它们是因为可以说大多数控制器方法都应该映射到特定的HTTP方法，而不是使用@RequestMapping（默认情况下，它会匹配所有HTTP方法）。在类级别上仍然需要@RequestMapping来表达共享的映射。

@RequestMapping 不能与同一元素（类、接口或方法）上声明的其他 @RequestMapping 注解一起使用。如果在同一元素上检测到多个 @RequestMapping 注解，将会记录警告信息，并且仅使用第一个映射。这也适用于组合的 @RequestMapping 注解，例如 @GetMapping、@PostMapping 等。

```java
@RestController
@RequestMapping("/persons")
class PersonController {

	@GetMapping("/{id}")
	public Person getPerson(@PathVariable Long id) {
		// ...
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void add(@RequestBody Person person) {
		// ...
	}
}
```

# 2 URI patterns
@RequestMapping 方法可以通过 URL 模式进行映射。有两种可选方案：
PathPattern - 一种预先解析的模式，用于与同样作为 PathContainer 预先解析的 URL 路径进行匹配。该方案专为 Web 应用设计，能有效处理编码和路径参数，并实现高效匹配。
AntPathMatcher - 将字符串模式与字符串路径进行匹配。这是 Spring 配置中用于选择类路径、文件系统及其他位置资源的原始方案。其效率较低，且字符串路径输入在处理 URL 编码及其他问题时存在挑战。
对于 Web 应用，推荐使用 PathPattern 方案，它也是 Spring WebFlux 中唯一的选择。自 Spring MVC 5.3 版本起支持使用 PathPattern，自 6.0 版本起默认启用。如需自定义路径匹配选项，请参阅 MVC 配置。


你可以使用通配符和全局模式来映射请求：
捕获的 URI 变量可以通过 @PathVariable 进行访问。例如：

```java

@GetMapping("/owners/{ownerId}/pets/{petId}")
public Pet findPet(@PathVariable Long ownerId, @PathVariable Long petId) {
	// ...
}
```
您可以在类和方法级别声明URI变量，如下例所示：


```java
@Controller
@RequestMapping("/owners/{ownerId}")
public class OwnerController {

	@GetMapping("/pets/{petId}")
	public Pet findPet(@PathVariable Long ownerId, @PathVariable Long petId) {
		// ...
	}
}

```

URI变量会自动转换为适当的类型，否则将抛出TypeMismatchException异常。默认情况下支持简单类型（如int、long、Date等），您也可以注册对其他数据类型的支持。请参阅类型转换和DataBinder。

您可以显式地为URI变量命名（例如@PathVariable("customId")），但如果变量名相同且代码使用-parameters编译器标志进行编译，则可以省略该细节。

语法{varName:regex}用于声明一个带有正则表达式的URI变量，其语法形式为{varName:regex}。例如，给定URL "/spring-web-3.0.5.jar"，以下方法可以提取名称、版本和文件扩展名：


```java

@GetMapping("/{name:[a-z-]+}-{version:\\d\\.\\d\\.\\d}{ext:\\.[a-z]+}")
public void handle(@PathVariable String name, @PathVariable String version, @PathVariable String ext) {
	// ...
}
```
URI路径模式也可以包含：
嵌入的${…}占位符，这些占位符在启动时通过PropertySourcesPlaceholderConfigurer根据本地、系统、环境和其他属性源进行解析。例如，这可用于基于外部配置对基础URL进行参数化。
SpEL表达式#{…}。


# 3 Pattern Comparison

当多个模式匹配一个URL时，必须选择最佳匹配项。具体选择方式取决于是否启用了解析后的PathPattern的使用，有以下两种方式之一：
PathPattern.SPECIFICITY_COMPARATOR
AntPathMatcher.getPatternComparator(String path)
这两种方式都有助于将模式按特定性排序，将更具体的模式排在前面。一个模式如果具有较少的URI变量（计为1）、单个通配符（计为1）和双通配符（计为2），则该模式更为具体。如果得分相同，则选择更长的模式。如果得分和长度都相同，则选择URI变量多于通配符的模式。

默认映射模式（/**）不参与评分，始终排在最后。此外，前缀模式（如/public/**）被认为比其他没有双通配符的模式更不具体。

如需了解完整细节，请参考上述链接中的模式比较器。

# 4 Suffix Match 后缀匹配

从 5.3 版本开始，默认情况下，Spring MVC 不再执行.* 后缀模式匹配，即如果控制器映射到 /person，它也会隐式地映射到 /person.*。因此，路径扩展名不再用于解释请求的响应内容类型——例如 /person.pdf、/person.xml 等。
在以前，浏览器发送的 Accept 头信息难以一致地解释时，使用文件扩展名是必要的。目前，这已不再是必需的，使用 Accept 头信息应是首选方式。
随着时间的推移，使用文件名扩展名已被证明在多个方面存在问题。当与 URI 变量、路径参数和 URI 编码的使用重叠时，它可能导致歧义。基于 URL 的授权和安全性的推理（详见下一节）也会变得更加困难。
要完全禁用 5.3 版本之前版本中路径扩展名的使用，请设置以下内容：
useSuffixPatternMatching(false)，参见 PathMatchConfigurer
favorPathExtension(false)，参见 ContentnegotiationConfigurer
通过“Accept”头信息之外的其他方式请求内容类型仍然是有用的，例如在浏览器中输入 URL 时。路径扩展名的一个安全替代方案是使用查询参数策略。如果必须使用文件扩展名，请考虑通过 ContentnegotiationConfigurer 的 mediaTypes 属性将其限制为显式注册的扩展名列表。

# 5 后缀匹配与RFD
反射式文件下载（RFD）攻击与XSS攻击类似，都依赖于请求输入（例如查询参数和URI变量）在响应中被反射。然而，RFD攻击并非将JavaScript插入HTML，而是依赖浏览器切换为执行下载操作，并在后续双击时将响应内容视为可执行脚本。

在Spring MVC中，`@ResponseBody`和`ResponseEntity`方法存在风险，因为它们可以渲染不同的内容类型，而客户端可以通过URL路径扩展名来请求这些内容类型。禁用后缀模式匹配并使用路径扩展名进行内容协商可以降低风险，但不足以完全防止RFD攻击。

为了防止RFD攻击，Spring MVC在渲染响应体之前，会添加一个`Content-Disposition:inline;filename=f.txt`头，以建议一个固定且安全的下载文件名。此操作仅在URL路径包含的文件扩展名既未被标记为安全，也未显式注册用于内容协商时才会执行。然而，当URL直接输入到浏览器中时，这可能会产生副作用。

默认情况下，许多常见的路径扩展名被标记为安全。具有自定义`HttpMessageConverter`实现的应用程序可以显式注册文件扩展名用于内容协商，以避免为这些扩展名添加`Content-Disposition`头。请参阅内容类型。

有关RFD的更多建议，请参阅CVE-2015-5211。

# 6 可消费的媒体类型 
查看 Reactive 栈中的等效内容 
您可以根据请求的 Content-Type 缩小请求映射的范围，如下例所示： 
```java
@PostMapping(path = "/pets", consumes = "application/json")
public void addPet(@RequestBody Pet pet) {
	// ...
}
```
使用 consumes 属性根据内容类型缩小映射范围。 
consumes 属性还支持否定表达式，例如 !text/plain 表示除 text/plain 以外的任何内容类型。 
您可以在类级别声明共享的 consumes 属性。然而，与大多数其他请求映射属性不同，当在类级别使用时，方法级别的 consumes 属性会覆盖而非扩展类级别的声明。 
MediaType 为常用的媒体类型提供了常量，例如 APPLICATION_JSON_VALUE 和 APPLICATION_XML_VALUE。


# 7.可生产媒体类型
请参阅反应式堆栈中的等效内容
您可以根据 `Accept` 请求头以及控制器方法所生产的内容类型列表来缩小请求映射范围，如下例所示：
```java
@GetMapping(path = "/pets/{petId}", produces = "application/json")
@ResponseBody
public Pet getPet(@PathVariable String petId) {
	// ...
}
```
使用 `produces` 属性按内容类型缩小映射范围。
媒体类型可以指定字符集。支持否定表达式，例如 `!text/plain` 表示除 "text/plain" 以外的任何内容类型。
您可以在类级别声明一个共享的 `produces` 属性。然而，与大多数其他请求映射属性不同，当在类级别使用时，方法级别的 `produces` 属性会覆盖而不是扩展类级别的声明。
`MediaType` 提供了常用媒体类型的常量，例如 `APPLICATION_JSON_VALUE` 和 `APPLICATION_XML_VALUE`。


# 8.Parameters, headers

您可以根据请求参数条件来缩小请求映射的范围。您可以测试某个请求参数是否存在（myParam）、是否不存在（!myParam），或者是否具有特定值（myParam=myValue）。以下示例展示了如何测试特定值：

```java

@GetMapping(path = "/pets/{petId}", params = "myParam=myValue")
public void findPet(@PathVariable String petId) {
	// ...
}
```
你也可以使用相同的方法来处理请求头条件，如下例所示：

```java

@GetMapping(path = "/pets/{petId}", headers = "myHeader=myValue")
public void findPet(@PathVariable String petId) {
	// ...
}
```
测试 myHeader 是否等于 myValue。

你可以使用头部条件来匹配 Content-Type 和 Accept，但最好还是使用 consumes 和 produces。

# 9.HTTP HEAD, OPTIONS


@GetMapping（以及@RequestMapping(method=HttpMethod.GET)）支持对请求映射透明地处理HTTP HEAD。控制器方法无需更改。在jakarta.servlet.http.HttpServlet中应用的一个响应包装器确保Content-Length头被设置为已写入的字节数（而不会实际写入响应）。

默认情况下，HTTP OPTIONS的处理方式是将Allow响应头设置为所有具有匹配URL模式的@RequestMapping方法中列出的HTTP方法列表。

对于未声明HTTP方法的@RequestMapping，Allow头将被设置为GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS。控制器方法应始终声明支持的HTTP方法（例如，通过使用特定HTTP方法的变体：@GetMapping、@PostMapping等）。

您可以显式地将@RequestMapping方法映射到HTTP HEAD和HTTP OPTIONS，但在一般情况下这是不必要的。

# 10. Custom Annotations

Spring MVC 支持使用组合注解来进行请求映射。这些注解本身被 `@RequestMapping` 元注解标注，并通过组合来重新声明 `@RequestMapping` 的一部分（或全部）属性，以实现更具体、更狭窄的目的。

`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping` 和 `@PatchMapping` 就是组合注解的示例。之所以提供这些注解，是因为可以说大多数控制器方法都应该映射到特定的 HTTP 方法，而不是使用 `@RequestMapping`（默认情况下它会匹配所有 HTTP 方法）。如果你需要了解如何实现组合注解的示例，可以查看这些注解是如何声明的。

`@RequestMapping` 不能与同一元素（类、接口或方法）上声明的其他 `@RequestMapping` 注解一起使用。如果在同一元素上检测到多个 `@RequestMapping` 注解，将会记录一个警告，并且只使用第一个映射。这也适用于 `@GetMapping`、`@PostMapping` 等组合的 `@RequestMapping` 注解。

Spring MVC 还支持自定义请求映射属性以及自定义请求匹配逻辑。这是一个更高级的选项，需要继承 `RequestMappingHandlerMapping` 并重写 `getCustomMethodCondition` 方法，在该方法中你可以检查自定义属性并返回自定义的 `RequestCondition`。


# 11 显式注册(Explicit Registrations)

你可以通过编程方式注册处理程序方法，这些方法可以用于动态注册或高级场景，例如在不同URL下注册同一处理程序的不同实例。以下示例注册了一个处理程序方法：



```java

@Configuration
public class MyConfig {

	@Autowired
	public void setHandlerMapping(RequestMappingHandlerMapping mapping, UserHandler handler) 1
			throws NoSuchMethodException {

		RequestMappingInfo info = RequestMappingInfo
				.paths("/user/{id}").methods(RequestMethod.GET).build(); 2

		Method method = UserHandler.class.getMethod("getUser", Long.class);3

		mapping.registerMapping(info, handler, method);4
	}
}
```

1. 注入目标处理器和控制器的处理器映射。
2. 准备请求映射元数据。
3. 获取处理器方法。
4. 添加注册信息。

# 12 @HttpExchange

虽然@HttpExchange的主要目的是通过生成的代理来抽象HTTP客户端代码，但承载这些注解的HTTP接口本身是与客户端或服务端使用无关的契约。除了简化客户端代码外，在某些情况下，HTTP接口也可能是服务端为客户端访问而暴露其API的一种便捷方式。这种方式会导致客户端与服务端之间的耦合度增加，通常并不是一个好的选择，尤其是对于公共API而言，但对于内部API来说，这可能正是目标。这种方法在Spring Cloud中经常使用，这也是为什么在控制器类中，@HttpExchange被作为@RequestMapping的替代方案，用于服务端处理。

```java

@HttpExchange("/persons")
interface PersonService {

	@GetExchange("/{id}")
	Person getPerson(@PathVariable Long id);

	@PostExchange
	void add(@RequestBody Person person);
}

@RestController
class PersonController implements PersonService {

	public Person getPerson(@PathVariable Long id) {
		// ...
	}

	@ResponseStatus(HttpStatus.CREATED)
	public void add(@RequestBody Person person) {
		// ...
	}
}

```
@HttpExchange 和 @RequestMapping 存在差异。@RequestMapping 可以通过路径模式、HTTP 方法等映射任意数量的请求，而 @HttpExchange 则声明一个具有具体 HTTP 方法、路径和内容类型的单一端点。

对于方法参数和返回值，通常来说，@HttpExchange 支持的方法参数是 @RequestMapping 的一个子集。值得注意的是，它排除了任何特定于服务端的参数类型。详情请参阅 @HttpExchange 和 @RequestMapping 的参数列表。

@HttpExchange 还支持一个 headers() 参数，该参数可以接受类似 "name=value" 的键值对，与客户端 @RequestMapping(headers={}) 的用法相同。在服务端，该参数支持 @RequestMapping 所支持的全部语法。


