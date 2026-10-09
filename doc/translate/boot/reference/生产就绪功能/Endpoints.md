# Endpoints

执行器端点允许您监控并与应用程序进行交互。Spring Boot 包含许多内置端点，并允许您添加自己的端点。例如，健康检查端点提供了应用程序的基本健康信息。您可以控制对每个单独端点的访问，并通过 HTTP 或 JMX 将其暴露（使其可远程访问）。当允许访问某个端点并且该端点被暴露时，该端点才被视为可用。内置端点仅在可用时才会自动配置。大多数应用程序选择通过 HTTP 暴露端点，其中端点的 ID 和 `/actuator` 的前缀会映射到一个 URL。例如，默认情况下，健康检查端点会映射到 `/actuator/health`。

要了解更多关于执行器端点及其请求和响应格式的信息，请参阅[API文档](https://docs.spring.io/spring-boot/api/rest/actuator/index.html)。

以下技术无关的端点可用：

|ID	|Description|
| --- | --- |
|auditevents|暴露当前应用程序的审计事件信息。需要一个 AuditEventRepository bean。|
|beans|显示应用程序中所有 Spring Bean 的完整列表。|
|caches|暴露可用的缓存。|
|conditions|显示在配置和自动配置类上评估的条件以及它们匹配或不匹配的原因。|
|configprops|显示所有@ConfigurationProperties的整理列表。需经过清理处理。|
|env|暴露来自Spring的ConfigurableEnvironment的属性。需进行清理处理。|
|flyway|显示已应用的任何 Flyway 数据库迁移。需要一个或多个 Flyway bean。|
|health|显示应用程序健康信息。|
|httpexchanges|显示 HTTP 交换信息（默认显示最近的 100 个 HTTP 请求-响应对）。需要一个 HttpExchangeRepository bean。|
|info|显示任意应用程序信息。|
|integrationgraph|显示 Spring Integration 图。需要依赖 spring-integration-core。|
|loggers|显示并修改应用程序中记录器的配置。|
| liquibase |显示已应用的任何 Liquibase 数据库迁移。需要一个或多个 Liquibase bean。|
| metrics |显示当前应用程序的“指标”信息，以诊断应用程序已记录的指标。|
| mappings|显示所有@RequestMapping路径的整理列表。|
| quartz |显示有关 Quartz Scheduler 作业的信息。需进行清理处理。|
| scheduledtasks |显示您应用程序中的计划任务。|
| sessions|允许从由 Spring Session 支持的会话存储中检索和删除用户会话。需要基于 servlet 的 Web 应用程序，并使用 Spring Session。|
| shutdown |允许应用程序优雅关闭。仅在使用jar打包时有效。默认禁用。|
| startup |显示由 ApplicationStartup 收集的启动步骤数据。要求 SpringApplication 配置为使用 BufferingApplicationStartup。|
| threaddump|执行线程转储。|

