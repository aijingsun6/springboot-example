# SpringApplication

`SpringApplication` 类提供了一种便捷的方式来引导从 `main()` 方法启动的 Spring 应用程序。在许多情况下，你可以委托给静态方法 `SpringApplication.run(Class, String...)`，如下例所示：

```java

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}

}
```

当你的应用程序启动时，你应该会看到类似以下内容的输出：

```java
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.1.1)

2026-08-17T10:51:43.606Z  INFO 11484 --- [           main] o.s.b.d.f.logexample.MyApplication       : Starting MyApplication using Java 25.0.4 with PID 11484 (/opt/apps/myapp.jar started by myuser in /opt/apps/)
2026-08-17T10:51:43.627Z  INFO 11484 --- [           main] o.s.b.d.f.logexample.MyApplication       : No active profile set, falling back to 1 default profile: "default"
2026-08-17T10:51:48.207Z  INFO 11484 --- [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat initialized with port 8080 (http)
2026-08-17T10:51:48.266Z  INFO 11484 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-08-17T10:51:48.275Z  INFO 11484 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/11.0.24]
2026-08-17T10:51:48.445Z  INFO 11484 --- [           main] b.w.c.s.WebApplicationContextInitializer : Root WebApplicationContext: initialization completed in 4488 ms
2026-08-17T10:51:50.163Z  INFO 11484 --- [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat started on port 8080 (http) with context path '/'
2026-08-17T10:51:50.190Z  INFO 11484 --- [           main] o.s.b.d.f.logexample.MyApplication       : Started MyApplication in 8.35 seconds (process running for 9.787)
2026-08-17T10:51:50.219Z  INFO 11484 --- [ionShutdownHook] o.s.boot.tomcat.GracefulShutdown         : Commencing graceful shutdown. Waiting for active requests to complete
2026-08-17T10:51:50.249Z  INFO 11484 --- [tomcat-shutdown] o.s.boot.tomcat.GracefulShutdown         : Graceful shutdown complete
```


498/5000
默认情况下，INFO级别的日志信息会显示出来，包括一些相关的启动细节，例如启动应用程序的用户。如果您需要设置其他日志级别，可以按照“日志级别”部分所述进行设置。应用程序版本是通过主应用程序类包中的实现版本来确定的。可以通过将 `spring.main.log-startup-info` 设置为 `false` 来关闭启动信息的日志记录。这样做也会关闭应用程序活动配置文件的日志记录。

要在启动过程中添加额外的日志记录，可以在 SpringApplication 的子类中重写 logStartupInfo(boolean) 方法。

# 启动失败(Startup Failure)

如果你的应用程序启动失败，注册的 FailureAnalyzer bean 将有机会提供专门的错误信息和具体的修复措施。例如，如果你在端口 8080 上启动一个 Web 应用程序，而该端口已被占用，你应该会看到类似以下的信息：
```java

***************************
APPLICATION FAILED TO START
***************************

Description:

Embedded servlet container failed to start. Port 8080 was already in use.

Action:

Identify and stop the process that is listening on port 8080 or configure this application to listen on another port.
```

Spring Boot 提供了众多 FailureAnalyzer 的实现，你也可以添加自己的实现。

如果没有任何故障分析器能够处理该异常，您仍然可以显示完整的条件报告，以便更好地了解问题所在。为此，您需要启用调试属性，或者为 `ConditionEvaluationReportLoggingListener` 启用 DEBUG 日志记录。

例如，如果您使用 `java -jar` 运行应用程序，可以按如下方式启用调试属性：

```java

$ java -jar myproject-0.0.1-SNAPSHOT.jar --debug
```

# 延迟初始化

SpringApplication 允许应用程序进行延迟初始化。启用延迟初始化后，Bean 会在需要时创建，而不是在应用程序启动时创建。因此，启用延迟初始化可以减少应用程序启动所需的时间。在 Web 应用程序中，启用延迟初始化会导致许多与 Web 相关的 Bean 在接收到 HTTP 请求之前不会被初始化。

延迟初始化的一个缺点是，它可能会延迟发现应用程序中的问题。如果某个配置错误的 Bean 是延迟初始化的，那么在启动时就不会发生故障，而问题只有在 Bean 被初始化时才会显现。此外，还必须确保 JVM 拥有足够的内存来容纳应用程序的所有 Bean，而不仅仅是那些在启动时初始化的 Bean。出于这些原因，默认情况下不会启用延迟初始化，建议在启用延迟初始化之前先对 JVM 的堆大小进行微调。

可以通过编程方式使用 SpringApplicationBuilder 的 lazyInitialization 方法或 SpringApplication 的 setLazyInitialization 方法来启用延迟初始化。或者，也可以使用 spring.main.lazy-initialization 属性来启用延迟初始化，如下例所示：

```java
spring.main.lazy-initialization=true
```

如果你想在禁用某些 bean 的延迟初始化功能的同时，对应用程序的其他部分使用延迟初始化，可以显式地使用 @Lazy(false) 注解将其 lazy 属性设置为 false。



# 自定义 Banner
启动时打印的横幅可以通过以下方式更改：将 banner.txt 文件添加到类路径中，或者将 spring.banner.location 属性设置为该文件的位置。如果该文件的编码不是 UTF-8，可以设置 spring.banner.charset。

在 banner.txt 文件中，您可以使用环境中可用的任何键，以及以下占位符中的任意一种：


如果您希望以编程方式生成横幅，可以使用 `SpringApplication.setBanner（…）` 方法。请使用 `Banner` 接口并实现您自己的 `printBanner()` 方法。 
您还可以使用 `spring.main.banner-mode` 属性来确定横幅是否需要打印到 `System.out`（控制台）、发送到配置的日志记录器（log），或者完全不生成（off）。 
打印的横幅会注册为一个名为 `springBootBanner` 的单例 bean。


`application.title`、`application.version` 和 `application.formatted-version` 属性仅在使用 `java -jar` 或 `java -cp` 与 Spring Boot 启动器时可用。如果您运行的是未解压的 JAR 文件，并通过 `java -cp <classpath> <mainclass>` 启动，或者将应用程序作为原生镜像运行，则这些值将不会被解析。

要使用 `application.*` 属性，请使用 `java -jar` 以打包 JAR 的形式启动应用程序，或使用 `java org.springframework.boot.loader.launch.JarLauncher` 以未解压 JAR 的形式启动。这将在构建类路径并启动应用程序之前初始化 `application.*` 横幅属性。


# 自定义 SpringApplication

如果 SpringApplication 的默认设置不符合您的喜好，您可以创建一个本地实例并进行自定义。例如，要关闭横幅，您可以这样写：


```java

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(MyApplication.class);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args);
	}

}
```

传递给 SpringApplication 的构造函数参数是 Spring Bean 的配置来源。在大多数情况下，这些参数是对 @Configuration 类的引用，但它们也可以是对 @Component 类的直接引用。 
也可以通过使用 application.properties 文件来配置 SpringApplication。详情请参阅外部化配置。 
如需完整的配置选项列表，请参阅 SpringApplication API 文档。

# 流畅构建器API(Fluent Builder API)

如果你需要构建一个 ApplicationContext 层次结构（具有父子关系的多个上下文），或者你更喜欢使用流畅的构建器 API，那么可以使用 SpringApplicationBuilder。 
SpringApplicationBuilder 允许你将多个方法调用链接在一起，并且包含创建父子关系的父方法和子方法，如下例所示：

```java

		new SpringApplicationBuilder().sources(Parent.class)
			.child(Application.class)
			.bannerMode(Banner.Mode.OFF)
			.run(args);
```

在创建 ApplicationContext 层次结构时存在一些限制。例如，Web 组件必须包含在子上下文中，并且父上下文和子上下文使用相同的 Environment。有关完整详细信息，请参阅 SpringApplicationBuilder API 文档。


# 应用程序可用性
当应用程序部署在平台之上时，它们可以通过 Kubernetes Probes 等基础设施向平台提供自身的可用性信息。Spring Boot 原生支持常用的“存活状态”（liveness）和“就绪状态”（readiness）两种可用性状态。如果您使用了 Spring Boot 的“actuator”支持，这些状态会以健康检查端点组的形式暴露出来。

此外，您还可以通过将 `ApplicationAvailability` 接口注入到自己的 Bean 中来获取可用性状态。

存活状态
应用程序的“存活状态”用于指示其内部状态是否允许其正常工作，或者如果当前出现故障，是否能够自行恢复。如果“存活状态”为故障状态，则意味着应用程序处于无法恢复的状态，基础设施应重新启动该应用程序。

通常，“存活状态”不应基于外部检查（例如健康检查）。如果依赖外部检查，一旦外部系统（如数据库、Web API、外部缓存）出现故障，就会触发平台范围内的大规模重启和级联故障。

Spring Boot 应用程序的内部状态主要由 Spring ApplicationContext 表示。如果应用程序上下文已成功启动，Spring Boot 会认为应用程序处于有效状态。一旦上下文被刷新，应用程序就被视为处于存活状态，详见 Spring Boot 应用程序生命周期和相关应用程序事件。

就绪状态
应用程序的“就绪状态”用于指示应用程序是否已准备好处理流量。如果“就绪状态”为故障状态，则平台应暂时不要将流量路由到该应用程序。这种情况通常发生在启动过程中，当正在处理 `CommandLineRunner` 和 `ApplicationRunner` 组件时，或者在应用程序认为当前太忙而无法处理更多流量时。

一旦应用程序和命令行运行器被调用，应用程序就被视为已就绪，详见 Spring Boot 应用程序生命周期和相关应用程序事件。

在启动期间需要执行的任务，应由 `CommandLineRunner` 和 `ApplicationRunner` 组件来执行，而不是使用 Spring 组件生命周期回调（如 `@PostConstruct`）。

管理应用程序可用性状态
应用程序组件可以随时通过注入 `ApplicationAvailability` 接口并调用其方法来获取当前的可用性状态。更常见的情况是，应用程序希望监听状态更新或更新应用程序的状态。

例如，我们可以将应用程序的“就绪状态”导出到文件中，以便 Kubernetes 的“exec Probe”可以查看该文件：

```java

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MyReadinessStateExporter {

	@EventListener
	public void onStateChange(AvailabilityChangeEvent<ReadinessState> event) {
		switch (event.getState()) {
			case ACCEPTING_TRAFFIC -> {
				// create file /tmp/healthy
			}
			case REFUSING_TRAFFIC -> {
				// remove file /tmp/healthy
			}
		}
	}

}
```

我们还可以在应用程序崩溃且无法恢复时更新应用程序的状态：

```java

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MyLocalCacheVerifier {

	private final ApplicationEventPublisher eventPublisher;

	public MyLocalCacheVerifier(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void checkLocalCache() {
		try {
			// ...
		}
		catch (CacheCompletelyBrokenException ex) {
			AvailabilityChangeEvent.publish(this.eventPublisher, ex, LivenessState.BROKEN);
		}
	}

}
```

Spring Boot 通过 Actuator 健康端点为 Kubernetes 提供了 "存活" 和 "就绪" HTTP 探测功能。您可以在专门的部分中获取更多关于在 Kubernetes 上部署 Spring Boot 应用程序的指导。

# 应用程序事件与监听器
除了 Spring 框架中常见的 ContextRefreshedEvent 等事件外，SpringApplication 还会发送一些额外的应用程序事件。
某些事件实际上是在 ApplicationContext 创建之前触发的，因此无法以 @Bean 的形式注册监听器。您可以通过 SpringApplication.addListeners（…） 方法或 SpringApplicationBuilder.listeners（…） 方法来注册这些监听器。
如果您希望这些监听器能够自动注册，无论应用程序以何种方式创建，都可以将一个 META-INF/spring.factories 文件添加到项目中，并使用 ApplicationListener 键引用您的监听器，如下例所示：
org.springframework.context.ApplicationListener=com.example.project.MyListener




应用程序事件按照以下顺序在应用程序运行时发送：

- 在运行开始时（但任何处理之前，除了监听器和初始化器的注册）会发送一个 `ApplicationStartingEvent`。
- 当上下文中要使用的环境已知但上下文尚未创建时，会发送一个 `ApplicationEnvironmentPreparedEvent`。
- 当 `ApplicationContext` 已准备就绪且 `ApplicationContextInitializers` 已被调用，但尚未加载任何 bean 定义时，会发送一个 `ApplicationContextInitializedEvent`。
- 在刷新开始之前（但 bean 定义已加载之后）会发送一个 `ApplicationPreparedEvent`。
- 在上下文刷新完成之后（但任何应用程序和命令行运行器被调用之前）会发送一个 `ApplicationStartedEvent`。
- 紧接着会发送一个带有 `LivenessState.CORRECT` 的 `AvailabilityChangeEvent`，以表明应用程序被视为存活。
- 在任何应用程序和命令行运行器被调用之后，会发送一个 `ApplicationReadyEvent`。
- 紧接着会发送一个带有 `ReadinessState.ACCEPTING_TRAFFIC` 的 `AvailabilityChangeEvent`，以表明应用程序已准备好处理请求。
- 如果启动过程中出现异常，则会发送一个 `ApplicationFailedEvent`。

上述列表仅包含与 `SpringApplication` 绑定的 `SpringApplicationEvent`。除了这些事件之外，在 `ApplicationPreparedEvent` 之后和 `ApplicationStartedEvent` 之前，还会发布以下事件：

- 在 Web 服务器准备就绪之后，会发送一个 `WebServerInitializedEvent`。`ServletWebServerInitializedEvent` 和 `ReactiveWebServerInitializedEvent` 分别是 Servlet 和反应式变体。
- 当 `ApplicationContext` 被刷新时，会发送一个 `ContextRefreshedEvent`。

你通常不需要使用应用程序事件，但了解它们的存在可能很有用。在内部，Spring Boot 使用事件来处理各种任务。事件监听器不应执行可能耗时较长的任务，因为它们默认在同一个线程中执行。可以考虑改用应用程序和命令行运行器。

应用程序事件是通过 Spring 框架的事件发布机制发送的。该机制的一部分确保了发布给子上下文中监听器的事件，也会发布给任何祖先上下文中的监听器。因此，如果你的应用程序使用了 SpringApplication 实例的层次结构，监听器可能会收到多个相同类型的应用程序事件实例。

为了让监听器能够区分其上下文的事件和后代上下文的事件，它应该请求注入其应用程序上下文，然后将注入的上下文与事件上下文进行比较。可以通过实现 `ApplicationContextAware` 接口来注入上下文，或者如果监听器是一个 bean，则可以使用 `@Autowired` 注解。


# Web 环境
SpringApplication 试图代表您创建正确类型的 ApplicationContext。用于确定 WebApplicationType 的算法如下：
如果存在 Spring MVC，则使用 AnnotationConfigServletWebServerApplicationContext
如果不存在 Spring MVC 但存在 Spring WebFlux，则使用 AnnotationConfigReactiveWebServerApplicationContext
否则，使用 AnnotationConfigApplicationContext
这意味着，如果您在同一应用程序中同时使用 Spring MVC 和 Spring WebFlux 中的新 WebClient，默认情况下将使用 Spring MVC。您可以通过调用 setWebApplicationType(WebApplicationType) 轻松覆盖此行为。
您也可以通过调用 setApplicationContextFactory（…） 来完全控制所使用的 ApplicationContext 类型。
在 JUnit 测试中使用 SpringApplication 时，通常希望调用 setWebApplicationType(WebApplicationType.NONE)。


# 访问应用程序参数 
如果需要访问传递给 `SpringApplication.run(...)` 的应用程序参数，可以注入一个 `ApplicationArguments` bean。`ApplicationArguments` 接口提供了对原始 `String[]` 参数以及解析后的选项参数和非选项参数的访问，如下例所示：

```java

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
public class MyBean {

	public MyBean(ApplicationArguments args) {
		boolean debug = args.containsOption("debug");
		List<String> files = args.getNonOptionArgs();
		if (debug) {
			System.out.println(files);
		}
		// if run with "--debug logfile.txt" prints ["logfile.txt"]
	}

}
```



Spring Boot 还会在 Spring 环境中注册一个 CommandLinePropertySource。这使得你也可以通过使用 @Value 注解来注入单个应用程序参数。

# 使用 ApplicationRunner / CommandLineRunner
如果你需要在 SpringApplication 启动后运行一些特定的代码，可以实现 `ApplicationRunner` 或 `CommandLineRunner` 接口。这两个接口的工作方式相同，都提供了一个 `run` 方法，该方法会在 `SpringApplication.run(...)` 完成之前被调用。


该合同非常适合在应用程序启动后但开始接受流量之前运行的任务。

CommandLineRunner 接口以字符串数组的形式提供对应用程序参数的访问，而 ApplicationRunner 则使用前面讨论过的 ApplicationArguments 接口。以下示例展示了一个带有 run 方法的 CommandLineRunner：

```java

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyCommandLineRunner implements CommandLineRunner {

	@Override
	public void run(String... args) {
		// Do something...
	}

}
```
如果定义了多个必须按特定顺序调用的 CommandLineRunner 或 ApplicationRunner Bean，您还可以实现 Ordered 接口或使用 Order 注解。

# 应用推出(Application Exit)

每个 SpringApplication 都会向 JVM 注册一个关闭钩子，以确保在退出时 ApplicationContext 能够优雅地关闭。所有标准的 Spring 生命周期回调（例如 DisposableBean 接口或 @PreDestroy 注解）都可以使用。

此外，如果希望在调用 SpringApplication.exit() 时返回特定的退出码，bean 可以实现 ExitCodeGenerator 接口。然后可以将该退出码传递给 System.exit()，以将其作为状态码返回，如下例所示：

```java
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MyApplication {

	@Bean
	public ExitCodeGenerator exitCodeGenerator() {
		return () -> 42;
	}

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(MyApplication.class, args)));
	}

}
```
此外，ExitCodeGenerator 接口也可以由异常实现。当遇到此类异常时，Spring Boot 会返回由实现的 getExitCode() 方法提供的退出码。 
如果存在多个 ExitCodeGenerator，则会使用生成的第一个非零退出码。为了控制生成器的调用顺序，可以额外实现 Ordered 接口或使用 Order 注解。


# 管理员功能

可以通过指定 `spring.application.admin.enabled` 属性来为应用程序启用与管理员相关的功能。这会在平台 MBeanServer 上暴露 `SpringApplicationAdminMXBean`。您可以使用此功能远程管理 Spring Boot 应用程序。该功能对于任何服务包装器实现也可能很有用。

如果您想知道应用程序正在哪个 HTTP 端口上运行，可以通过键 `local.server.port` 获取该属性。

# 应用程序启动跟踪

在应用程序启动过程中，SpringApplication 和 ApplicationContext 会执行许多与应用程序生命周期、Bean 生命周期甚至处理应用程序事件相关的任务。通过 ApplicationStartup，Spring 框架允许您使用 StartupStep 对象来跟踪应用程序的启动顺序。这些数据可以用于性能分析，或者仅仅是为了更好地理解应用程序的启动过程。

在设置 SpringApplication 实例时，您可以选择一个 ApplicationStartup 的实现。例如，要使用 BufferingApplicationStartup，您可以这样写：

```java

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

@SpringBootApplication
public class MyApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(MyApplication.class);
		application.setApplicationStartup(new BufferingApplicationStartup(2048));
		application.run(args);
	}

}
```
第一个可用的实现是 Spring Framework 提供的 FlightRecorderApplicationStartup。它将 Spring 特定的启动事件添加到 Java Flight Recorder 会话中，用于分析应用程序并将其 Spring 上下文生命周期与 JVM 事件（如分配、GC、类加载等）关联起来。配置完成后，您可以通过启用 Flight Recorder 运行应用程序来记录数据：


```java

$ java -XX:StartFlightRecording:filename=recording.jfr,duration=10s -jar demo.jar
```
Spring Boot 附带了 `BufferingApplicationStartup` 变体；该实现用于缓冲启动步骤并将其排入外部指标系统。应用程序可以在任何组件中请求类型为 `BufferingApplicationStartup` 的 bean。 
Spring Boot 还可以配置为暴露一个启动端点，该端点以 JSON 文档的形式提供此信息。


# 虚拟线程(Virtual threads)
虚拟线程需要 Java 21 或更高版本。为了获得最佳体验，强烈建议使用 Java 24 或更高版本。要启用虚拟线程，请将 `spring.threads.virtual.enabled` 属性设置为 `true`。

在为您的应用程序启用此选项之前，您应考虑阅读官方的 Java 虚拟线程文档。在某些情况下，应用程序可能会因为“被钉住的虚拟线程”而出现吞吐量降低的问题；本页面还解释了如何使用 JDK Flight Recorder 或 jcmd CLI 来检测此类情况。

如果启用了虚拟线程，配置线程池的属性将不再生效。这是因为虚拟线程是在JVM范围的通用平台线程池中进行调度，而不是在专用的线程池中。

虚拟线程的一个副作用是它们是守护线程。如果JVM中的所有线程都是守护线程，JVM将会退出。当你依赖`@Scheduled`注解的Bean来保持应用程序存活时，这种行为可能会成为问题。如果你使用了虚拟线程，调度线程本身就是一个虚拟线程，因此也是一个守护线程，无法保持JVM的存活。这不仅会影响调度，也可能对其他技术产生类似影响。为了确保在所有情况下JVM都能继续运行，建议将`spring.main.keep-alive`属性设置为`true`。这样即使所有线程都是虚拟线程，也能确保JVM保持存活。



# 参考文档
- [virtual-threads](https://docs.oracle.com/en/java/javase/24/core/virtual-threads.html)
- 
