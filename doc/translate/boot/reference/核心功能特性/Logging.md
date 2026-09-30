# Logging

Spring Boot 在所有内部日志记录中都使用 Commons Logging，但底层日志实现是开放的。默认配置支持 Java Util Logging、Log4j2 和 Logback。在每种情况下，日志记录器都预先配置为使用控制台输出，同时也可以选择性地使用文件输出。

默认情况下，如果您使用了起步依赖（starters），则日志记录将使用 Logback。Spring Boot 还包含了适当的 Logback 路由配置，以确保使用 Java Util Logging、Commons Logging、Log4J 或 SLF4J 的依赖库都能正常工作。

TIP:
Java 有很多可用的日志框架。如果上面的列表看起来令人困惑，请不要担心。通常情况下，你不需要更改日志依赖项，Spring Boot 的默认设置已经足够好了。


当你将应用程序部署到servlet容器或应用服务器时，使用Java Util Logging API进行的日志记录不会被路由到你的应用程序日志中。这样可以防止容器或部署到其中的其他应用程序执行的日志记录出现在你的应用程序日志中。

# 1. Log Format
Spring Boot 的默认日志输出类似于以下示例：
```java
2026-08-17T10:51:28.508Z  INFO 11390 --- [myapp] [           main] o.s.b.d.f.logexample.MyApplication       : Starting MyApplication using Java 25.0.4 with PID 11390 (/opt/apps/myapp.jar started by myuser in /opt/apps/)
2026-08-17T10:51:28.527Z  INFO 11390 --- [myapp] [           main] o.s.b.d.f.logexample.MyApplication       : No active profile set, falling back to 1 default profile: "default"
2026-08-17T10:51:33.118Z  INFO 11390 --- [myapp] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat initialized with port 8080 (http)
2026-08-17T10:51:33.232Z  INFO 11390 --- [myapp] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-08-17T10:51:33.239Z  INFO 11390 --- [myapp] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/11.0.24]
2026-08-17T10:51:33.461Z  INFO 11390 --- [myapp] [           main] b.w.c.s.WebApplicationContextInitializer : Root WebApplicationContext: initialization completed in 4632 ms
2026-08-17T10:51:35.255Z  INFO 11390 --- [myapp] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat started on port 8080 (http) with context path '/'
2026-08-17T10:51:35.286Z  INFO 11390 --- [myapp] [           main] o.s.b.d.f.logexample.MyApplication       : Started MyApplication in 8.572 seconds (process running for 10.155)
2026-08-17T10:51:35.315Z  INFO 11390 --- [myapp] [ionShutdownHook] o.s.boot.tomcat.GracefulShutdown         : Commencing graceful shutdown. Waiting for active requests to complete
2026-08-17T10:51:35.346Z  INFO 11390 --- [myapp] [tomcat-shutdown] o.s.boot.tomcat.GracefulShutdown         : Graceful shutdown complete
```
以下项目将被输出：
- 日期和时间：毫秒级精度，易于排序。
- 日志级别：ERROR、WARN、INFO、DEBUG 或 TRACE。
- 进程 ID。
- 一个 --- 分隔符，用于区分实际日志消息的开始。
- 应用程序名称：用方括号括起（仅在设置了 spring.application.name 时默认记录）。
- 应用程序组：用方括号括起（仅在设置了 spring.application.group 时默认记录）。
- 线程名称：用方括号括起（控制台输出时可能会被截断）。
- 关联 ID：如果启用了追踪（在上面的示例中未显示）。
- 记录器名称：这通常是源类名（通常会缩写）。
- 日志消息。

Logback 没有 FATAL 级别，它被映射为 ERROR。

如果你有一个 spring.application.name 属性，但不想将其记录到日志中，可以将 logging.include-application-name 设置为 false。

如果您有 `spring.application.group` 属性但不想将其记录在日志中，可以将 `logging.include-application-group` 设置为 `false`。

有关关联ID的更多详细信息，请参阅此[文档](https://docs.spring.io/spring-boot/reference/actuator/tracing.html#actuator.micrometer-tracing.logging)。


# 2. Console Output

默认的日志配置会将消息写入时回显到控制台。默认情况下，会记录 ERROR 级别、WARN 级别和 INFO 级别的消息。您还可以通过使用 --debug 标志启动应用程序来启用“调试”模式。

```java
$ java -jar myapp.jar --debug
```

你也可以在 application.properties 中指定 debug=true。


启用调试模式时，会配置部分核心日志记录器（嵌入式容器、Hibernate 和 Spring Boot）以输出更多信息。启用调试模式并不会使应用程序将所有消息都记录为 DEBUG 级别。

另外，您可以通过在启动应用程序时添加 --trace 标志（或在 application.properties 中设置 trace=true）来启用“跟踪”模式。这样做会为部分核心日志记录器（嵌入式容器、Hibernate 模式生成以及整个 Spring 框架）启用跟踪日志记录。

如果您想禁用基于控制台的日志记录，可以将 logging.console.enabled 属性设置为 false。

# 3. Color-coded Output

如果您的终端支持 ANSI，则会使用颜色输出以提高可读性。您可以将 `spring.output.ansi.enabled` 设置为支持的值，以覆盖自动检测。

颜色编码通过使用 `%clr` 转换词进行配置。在最简单的形式中，该转换器会根据日志级别为输出着色，如下例所示：

```java
%clr(%5p)
```
日志级别与颜色对应关系如下表所示：

| Level	|Color |
| --- | --- |
| FATAL|Red|
|ERROR|Red|
|WARN|Yellow|
|INFO|Green|
|DEBUG|Green|
|TRACE|Green|

或者，您可以通过将颜色和样式作为选项提供给转换过程来指定应使用的颜色和样式。例如，要将文本设置为黄色并加粗，可以使用以下设置：

```java
%clr(%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX}){yellow,bold}
```

支持以下文本颜色：

- black
- blue
- bright_black
- bright_blue
- bright_cyan
- bright_green
- bright_magenta
- bright_red
- bright_white
- bright_yellow
- cyan
- green
- magenta
- red
- white
- yellow

支持以下背景色：

bg_black

bg_blue

bg_bright_black

bg_bright_blue

bg_bright_cyan

bg_bright_green

bg_bright_magenta

bg_bright_red

bg_bright_white

bg_bright_yellow

bg_cyan

bg_green

bg_magenta

bg_red

bg_white

bg_yellow

支持以下样式：

bold

faint

italic

normal

reverse

underline

# 4. File Output

默认情况下，Spring Boot 仅将日志输出到控制台，而不写入日志文件。如果您希望在控制台输出的基础上再写入日志文件，需要设置 `logging.file.name` 或 `logging.file.path` 属性（例如在 `application.properties` 文件中）。如果同时设置了这两个属性，`logging.file.path` 将被忽略，仅使用 `logging.file.name`。

下表展示了 `logging.*` 属性如何组合使用：
|logging.file.name|	logging.file.path|	Description|
| --- | --- | --- |
|(none)|(none)|仅控制台日志记录。|
|Specific file (for example, my.log)|(none)|写入到由 logging.file.name 指定的位置。该位置可以是绝对路径，也可以是相对于当前目录的路径。|
|(none)|Specific directory (for example, /var/log)|将spring.log写入logging.file.path指定的目录，该目录可以是绝对路径，也可以是相对于当前目录的路径。|
|Specific file|Specific directory|写入由 logging.file.name 指定的位置，并忽略 logging.file.path。该位置可以是绝对路径，也可以是相对于当前目录的路径。|

当日志文件达到10 MB时，日志文件会进行轮转，与控制台输出一样，默认会记录ERROR级别、WARN级别和INFO级别的消息。请注意，Log4J2要求使用此类配置时必须设置logging.file.path。

NOTE:
日志属性与实际的日志基础设施是独立的。因此，特定的配置键（例如 Logback 的 logback.configurationFile）不由 Spring Boot 管理。

# 5. File Rotation

如果你使用的是Logback或Log4j 2，可以通过application.properties或application.yaml文件来微调日志轮转设置。对于其他所有日志系统，你需要自行直接配置轮转设置。
以下轮转策略属性适用于Logback：


|Name |	Description|
| --- | --- |
|logging.logback.rollingpolicy.file-name-pattern|用于创建日志归档的文件名模式。|
|logging.logback.rollingpolicy.clean-history-on-start|是否应在应用程序启动时执行日志归档清理。|
|logging.logback.rollingpolicy.max-file-size|日志文件在归档前的最大大小。|
|logging.logback.rollingpolicy.total-size-cap|在删除之前，日志归档文件可以占用的最大大小。|
|logging.logback.rollingpolicy.max-history|要保留的归档日志文件的最大数量（默认为7）。|

如果你使用的是Log4j2，以下轮转策略属性可用：

|Name|	Description|
| --- | --- |
|logging.log4j2.rollingpolicy.file-name-pattern|用于创建日志归档的文件名模式。|
|logging.log4j2.rollingpolicy.max-file-size|The maximum size of log file before it is archived (defaults to 10MB).|
|logging.log4j2.rollingpolicy.max-history|The maximum number of archive log files to keep (defaults to 7).
|logging.log4j2.rollingpolicy.strategy|Rolling policy strategy (defaults to 'size').
|logging.log4j2.rollingpolicy.cron|Cron expression used when the strategy is 'cron' (default to every day at midnight).
|logging.log4j2.rollingpolicy.time-interval|Time based triggering interval when the strategy is 'time' or 'size-and-time' (default to 1).
|logging.log4j2.rollingpolicy.time-modulate|Whether to align the next rollover time to occur at the top of the interval when the strategy is time based (defaults to false).

# 6. Log Levels

所有支持的日志系统都可以在 Spring 环境（例如在 application.properties 中）中通过使用 `logging.level.<logger-name>=<level>` 来设置日志记录器的级别，其中 `level` 可以是 TRACE、DEBUG、INFO、WARN、ERROR、FATAL 或 OFF。根日志记录器可以通过 `logging.level.root` 进行配置。

以下示例展示了 application.properties 中可能的日志设置：


```java

logging.level.root=warn
logging.level.org.springframework.web=debug
logging.level.org.hibernate=error
```

也可以通过环境变量来设置日志级别。例如，LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB=DEBUG 会将 org.springframework.web 设置为 DEBUG。

上述方法仅适用于包级别的日志记录。由于宽松绑定总是将环境变量转换为小写，因此无法通过这种方式为单个类配置日志记录。如果需要为某个类配置日志记录，可以使用SPRING_APPLICATION_JSON变量。


# 7. Log Groups

通常，能够将相关的日志记录器分组在一起以便同时进行配置是非常有用的。例如，你可能经常需要更改所有与 Tomcat 相关的日志记录器的日志级别，但你却不容易记住顶级包名。

为了解决这个问题，Spring Boot 允许你在 Spring 环境中定义日志记录组。例如，你可以通过在 application.properties 中添加以下内容来定义一个 "tomcat" 组：

```java

logging.group.tomcat=org.apache.catalina,org.apache.coyote,org.apache.tomcat
```
一旦定义好，你可以用一行代码更改该组中所有记录器的级别：
```java
logging.level.tomcat=trace
```
Spring Boot 包含以下预定义的日志组，可以开箱即用：

|Name |	Loggers|
| --- | --- |
| web |org.springframework.core.codec, org.springframework.http, org.springframework.web, org.springframework.boot.actuate.endpoint.web, org.springframework.boot.web.servlet.ServletContextInitializerBeans
|sql|org.springframework.jdbc.core, org.hibernate.SQL, LoggerListener

# 8. Using a Log Shutdown Hook
为了在应用程序终止时释放日志记录资源，提供了一个关闭钩子，该钩子将在JVM退出时触发日志系统清理。除非您的应用程序部署为war文件，否则该关闭钩子会自动注册。如果您的应用程序具有复杂的上下文层次结构，关闭钩子可能无法满足您的需求。如果无法满足，请禁用关闭钩子并调查底层日志系统直接提供的选项。例如，Logback提供了上下文选择器，允许每个Logger在其自己的上下文中创建。您可以使用logging.register-shutdown-hook属性禁用关闭钩子。将其设置为false将禁用注册。您可以在application.properties或application.yaml文件中设置该属性：

```java
logging.register-shutdown-hook=false
```
# 9. Custom Log Configuration

各种日志系统可以通过在类路径中包含相应的库来激活，并且可以通过在类路径的根目录或由以下 Spring 环境属性指定的位置提供合适的配置文件来进一步自定义。 
您可以通过使用 `org.springframework.boot.logging.LoggingSystem` 系统属性来强制 Spring Boot 使用特定的日志系统。该属性的值应为 `LoggingSystem` 实现类的完全限定类名。您也可以通过将值设置为 `none` 来完全禁用 Spring Boot 的日志配置。

由于日志记录在 ApplicationContext 创建之前就已经初始化，因此无法通过 Spring @Configuration 文件中的 @PropertySources 控制日志记录。唯一可以更改日志系统或完全禁用日志的方法是通过系统属性。

根据您的日志系统，以下文件已被加载：

|Logging System	|Customization|
| --- | --- |
|Logback|logback-spring.xml, logback-spring.groovy, logback.xml, or logback.groovy
|Log4j2|log4j2-spring.xml or log4j2.xml
|JDK (Java Util Logging)|logging.properties

在可能的情况下，我们建议您使用带有 `-spring` 后缀的变体进行日志配置（例如，使用 `logback-spring.xml` 而不是 `logback.xml`）。如果您使用标准的配置位置，Spring 将无法完全控制日志的初始化。

Java Util Logging 在从“可执行 jar”运行时存在已知的类加载问题，会导致运行异常。如果可能，我们建议您在这种情况下避免使用它。

为了帮助进行自定义，一些其他属性会从 Spring 环境转移到系统属性中。这使得这些属性可以被日志系统配置所使用。例如，在 `application.properties` 中设置 `logging.file.name` 或将 `LOGGING_FILE_NAME` 作为环境变量，都会导致 `LOG_FILE` 系统属性被设置。转移的属性在以下表格中进行了说明：

如果你想在日志属性中使用占位符，应该使用 Spring Boot 的语法，而不是底层框架的语法。特别需要注意的是，如果你使用的是 Logback，你应该使用 `:` 作为属性名与其默认值之间的分隔符，而不是使用 `:-`。


您可以通过仅覆盖 LOG_LEVEL_PATTERN（或在 Logback 中使用 logging.pattern.level）来将 MDC 和其他临时内容添加到日志行中。例如，如果您使用 logging.pattern.level=user:%X{user} %5p，那么默认日志格式将包含一个“user”的 MDC 条目（如果存在），如下例所示。

```java

2019-08-30 12:30:04.031 user:someone INFO 22174 --- [  nio-8080-exec-0] demo.Controller
Handling authenticated request
```

# 10. Structured Logging

结构化日志是一种技术，其中日志输出以定义良好的、通常可由机器读取的格式编写。Spring Boot 支持结构化日志，并且开箱即用地支持以下 JSON 格式：
- [Elastic Common Schema (ECS)](https://docs.spring.io/spring-boot/reference/features/logging.html#features.logging.structured.ecs)
- [Graylog 扩展日志格式 (GELF)]()
- [Logstash]()

要启用结构化日志记录，请将属性 logging.structured.format.console（用于控制台输出）或 logging.structured.format.file（用于文件输出）设置为您要使用的格式标识符。 
如果您使用的是自定义日志配置，请更新您的配置以支持 CONSOLE_LOG_STRUCTURED_FORMAT 和 FILE_LOG_STRUCTURED_FORMAT 系统属性。以 CONSOLE_LOG_STRUCTURED_FORMAT 为例：

logback
```xml
<!-- replace your encoder with StructuredLogEncoder -->
<encoder class="org.springframework.boot.logging.logback.StructuredLogEncoder">
	<format>${CONSOLE_LOG_STRUCTURED_FORMAT}</format>
	<charset>${CONSOLE_LOG_CHARSET}</charset>
</encoder>
```
log4j
```xml
<!-- replace your PatternLayout with StructuredLogLayout -->
<StructuredLogLayout format="${sys:CONSOLE_LOG_STRUCTURED_FORMAT}" charset="${sys:CONSOLE_LOG_CHARSET}"/>
```
您也可以参考 Spring Boot 中包含的默认配置：
- [Log4j2 控制台附加器](https://github.com/spring-projects/spring-boot/blob/v4.1.1/core/spring-boot/src/main/resources/org/springframework/boot/logging/log4j2/log4j2.xml)
- [Log4j2 控制台和文件附加器](https://github.com/spring-projects/spring-boot/blob/v4.1.1/core/spring-boot/src/main/resources/org/springframework/boot/logging/log4j2/log4j2-file.xml)

## 10.1 [Elastic Common Schema](https://www.elastic.co/guide/en/ecs/8.11/ecs-reference.html)

[Elastic Common Schema](https://www.elastic.co/guide/en/ecs/8.11/ecs-reference.html)是一种基于JSON的日志记录格式。


要启用弹性通用模式（Elastic Common Schema）日志格式，请将相应的格式属性设置为 ecs：

```java
logging.structured.format.console=ecs
logging.structured.format.file=ecs
```

日志行看起来像这样：

```java

{"@timestamp":"2024-01-01T10:15:00.067462556Z","log":{"level":"INFO","logger":"org.example.Application"},"process":{"pid":39599,"thread":{"name":"main"}},"service":{"name":"simple"},"message":"No active profile set, falling back to 1 default profile: \"default\"","ecs":{"version":"8.11"}}
```






# 参考文档
- [Logging](https://docs.spring.io/spring-boot/reference/features/logging.html)