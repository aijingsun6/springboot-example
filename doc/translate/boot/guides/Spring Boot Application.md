# Spring Boot Application

本节包含与 Spring Boot 应用程序直接相关的主题。


# 创建您自己的 FailureAnalyzer

FailureAnalyzer 是一种在启动时拦截异常并将其转换为人类可读消息的绝佳方式，该消息被封装在一个 FailureAnalysis 中。Spring Boot 提供了针对应用程序上下文相关异常、JSR-303 验证等的此类分析器。你也可以创建自己的分析器。

AbstractFailureAnalyzer 是 FailureAnalyzer 的一个便捷扩展，它检查待处理异常中是否存在指定的异常类型。你可以从该抽象类扩展，以便你的实现仅在实际存在该异常时才处理它。如果由于某种原因你无法处理该异常，请返回 null，以便让其他实现有机会处理该异常。

FailureAnalyzer 的实现必须注册在 META-INF/spring.factories 中。以下示例注册了 ProjectConstraintViolationFailureAnalyzer：


```bash

org.springframework.boot.diagnostics.FailureAnalyzer=\
com.example.ProjectConstraintViolationFailureAnalyzer
```

如果你需要访问 BeanFactory 或 Environment，请在 FailureAnalyzer 的实现中将它们声明为构造函数参数。


# 自动配置故障排除(Troubleshoot Auto-configuration)

Spring Boot 自动配置会尽力“做正确的事”，但有时配置会失败，且难以确定原因。

在任何 Spring Boot 的 ApplicationContext 中，都有一个非常实用的 ConditionEvaluationReport。启用 DEBUG 级别日志输出即可查看该报告。如果您使用了 spring-boot-actuator（请参阅 Actuator 章节），还有一个 conditions 端点，可将该报告以 JSON 格式呈现。利用该端点可以调试应用程序，查看 Spring Boot 在运行时添加了哪些功能（以及哪些功能未被添加）。

更多问题可以通过查阅源代码和 API 文档得到解答。阅读代码时，请牢记以下经验法则：

查找名为 *AutoConfiguration 的类并阅读其源代码。特别关注 @Conditional* 注解，以了解它们启用了哪些功能以及在何种条件下启用。在命令行中添加 --debug 参数，或设置系统属性 -Ddebug，即可在控制台日志中查看应用程序所做的所有自动配置决策。在启用了 actuator 的运行中应用程序中，可查看 conditions 端点（/actuator/conditions 或其 JMX 等效端点）获取相同信息。

查找带有 @ConfigurationProperties 注解的类（例如 ServerProperties），从中了解可用的外部配置选项。@ConfigurationProperties 注解包含一个 name 属性，该属性作为外部属性的前缀。因此，ServerProperties 的前缀为 "server"，其配置属性包括 server.port、server.address 等。在启用了 actuator 的运行中应用程序中，可查看 configprops 端点。

查找在 Binder 上使用 bind 方法的地方，该方法以宽松的方式从 Environment 中显式提取配置值。该方法通常与一个前缀配合使用。

查找直接绑定到 Environment 的 @Value 注解。

查找 @ConditionalOnExpression 注解，该注解根据 SpEL 表达式（通常使用从 Environment 解析的占位符进行求值）来开启或关闭功能。

# 在启动前自定义环境或 ApplicationContext(Customize the Environment or ApplicationContext Before It Starts)

`SpringApplication` 提供了 `ApplicationListener` 和 `ApplicationContextInitializer` 的实现，用于对上下文或环境进行自定义配置。Spring Boot 会从 `META-INF/spring.factories` 中加载一些此类自定义配置，供内部使用。注册额外的自定义配置有不止一种方式：

- 以编程方式，针对每个应用程序，在运行 `SpringApplication` 之前调用其 `addListeners` 和 `addInitializers` 方法。
- 以声明方式，针对所有应用程序，通过添加 `META-INF/spring.factories` 文件并打包一个 JAR 文件，供所有应用程序作为库使用。

`SpringApplication` 会向监听器发送一些特殊的 `ApplicationEvent`（有些甚至在上下文创建之前就发送了），然后还会为 `ApplicationContext` 发布的事件注册这些监听器。完整的列表请参见“Spring Boot 特性”部分中的“应用程序事件和监听器”。

此外，还可以在应用程序上下文刷新之前，通过 `EnvironmentPostProcessor` 来定制 `Environment`。每个实现都应注册到 `META-INF/spring.factories` 中，如下例所示：

```
org.springframework.boot.EnvironmentPostProcessor=com.example.YourEnvironmentPostProcessor
```

该实现可以加载任意文件并将其添加到 `Environment` 中。例如，以下示例从类路径中加载一个 YAML 配置文件：

```yaml

import java.io.IOException;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class MyEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Resource path = new ClassPathResource("com/example/myapp/config.yml");
		PropertySource<?> propertySource = loadYaml(path);
		environment.getPropertySources().addLast(propertySource);
	}

	private PropertySource<?> loadYaml(Resource path) {
		Assert.isTrue(path.exists(), () -> "'path' [%s] must exist".formatted(path));
		try {
			return this.loader.load("custom-resource", path).get(0);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to load yaml configuration from " + path, ex);
		}
	}

}
```

环境已经预先配置了所有 Spring Boot 默认加载的常规属性源。因此，可以从环境中获取文件的位置。前面的示例将自定义资源属性源添加到列表末尾，这样，在任何其他常规位置定义的键都将具有优先权。自定义实现可以定义另一种顺序。

虽然在你的 `@SpringBootApplication` 上使用 `@PropertySource` 似乎是一种在环境中加载自定义资源的便捷方式，但我们并不推荐这样做。这些属性源直到应用程序上下文被刷新时才会被添加到环境中。这对于配置某些属性（例如 logging.* 和 spring.main.*）来说为时已晚，因为这些属性在刷新开始之前就已经被读取了。


# 构建 ApplicationContext 层次结构（添加父上下文或根上下文)

你可以使用 SpringApplicationBuilder 类来创建父子 ApplicationContext 层次结构。有关更多信息，请参阅“Spring Boot 功能”部分中的 Fluent Builder API。



# Create a Non-web Application

并非所有 Spring 应用程序都必须成为 Web 应用程序（或 Web 服务）。如果您希望在 main 方法中执行一些代码，同时又希望引导一个 Spring 应用程序来设置所需的基础设施，那么您可以使用 Spring Boot 的 SpringApplication 特性。SpringApplication 会根据它是否认为需要一个 Web 应用程序来更改其 ApplicationContext 类。您首先可以做的，就是将与服务器相关的依赖项（例如 servlet API）从类路径中移除。如果您无法做到这一点（例如，如果您从同一个代码库运行两个应用程序），那么您可以显式地在 SpringApplication 实例上调用 setWebApplicationType(WebApplicationType.NONE)，或者通过 Java API 或外部属性设置 applicationContextClass 属性。您希望作为业务逻辑运行的代码，可以以 CommandLineRunner 的形式实现，并作为 @Bean 定义放入上下文中。