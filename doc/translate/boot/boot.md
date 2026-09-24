# boot

Spring Boot 帮助您创建可运行的独立、生产级 Spring 应用程序。我们对 Spring 平台和第三方库持有明确的观点，以便您能以最少的麻烦快速上手。大多数 Spring Boot 应用程序几乎不需要任何 Spring 配置。

您可以使用 Spring Boot 创建 Java 应用程序，这些应用程序可以通过 `java -jar` 命令启动，也可以采用更传统的 WAR 部署方式。

我们的主要目标是：
为所有 Spring 开发提供一个显著更快且易于上手的体验。
默认情况下提供明确的观点，但当需求开始偏离默认设置时，能迅速让路。
提供一系列适用于大多数项目类型的非功能性特性（如嵌入式服务器、安全性、指标监控、健康检查和外部化配置）。
完全不需要代码生成（除非针对原生镜像），也无需 XML 配置。

# Community

如果您在使用 Spring Boot 时遇到问题，我们很乐意提供帮助。 
请尝试查阅[操作指南](https://docs.spring.io/spring-boot/how-to/index.html)文档，它们提供了针对常见问题的解决方案。 
学习 Spring 的基础知识。Spring Boot 是基于许多其他 Spring 项目构建的。您可以访问 spring.io 网站获取丰富的参考文档。如果您刚开始接触 Spring，可以尝试阅读其中的某份[指南](https://spring.io/guides/)。 
提出问题。我们会在 [stackoverflow.com](https://stackoverflow.com/questions) 上监控标记为 [spring-boot](https://stackoverflow.com/tags/spring-boot) 的问题。 
在 github.com/spring-projects/spring-boot/issues 上报告 Spring Boot 的问题。


# System Requirements

Spring Boot 4.1.1 至少需要 Java 17，并且兼容至 Java 26 版本。同时还需要 Spring Framework 7.0.9 或更高版本。 
第三方项目的支持可能具有额外或更高的要求。请参阅相关支持的文档以获取更多详细信息。 
以下构建工具提供了显式的构建支持： 
|构建工具 |版本 |
| --- | --- | 
|Maven |3.6.3 或更高版本| 
|Gradle |Gradle 8.x（8.14 或更高版本）和 9.x| 

Servlet 容器 
Spring Boot 支持以下嵌入式 Servlet 容器： 
|名称 |Servlet 版本| 
| --- | --- |
|Tomcat11.0.x| 6.1| 
|Jetty 12.1.x |6.1| 
您也可以将 Spring Boot 应用程序部署到任何兼容 Servlet 6.1+ 的容器中。 
GraalVM 原生镜像 
Spring Boot 应用程序可以使用 GraalVM 25 或更高版本转换为原生镜像。 
可以使用原生构建工具 Gradle/Maven 插件或 GraalVM 提供的 native-image 工具来创建镜像。您也可以使用 native-image Paketo 构建包来创建原生镜像。 
支持以下版本： 
|名称 |版本| 
|---|---|
|GraalVM 社区版 |25|
|原生构建工具 |1.1.8|


# 安装
Spring Boot 可以与“经典”的 Java 开发工具一起使用，也可以作为命令行工具安装。无论哪种方式，您都需要 Java SDK v17 或更高版本。在开始之前，您应该使用以下命令检查当前的 Java 安装情况：
```bash
$ java -version
```

如果您是 Java 开发的新手，或者想尝试使用 Spring Boot，可以先试试 Spring Boot CLI（命令行界面）。否则，请继续阅读“经典”安装说明。

Java 开发者的安装说明
您可以像使用任何标准 Java 库一样使用 Spring Boot。为此，请在类路径中包含适当的 spring-boot-*.jar 文件。Spring Boot 不需要任何特殊的工具集成，因此您可以使用任何 IDE 或文本编辑器。此外，Spring Boot 应用程序本身也没有任何特殊之处，因此您可以像运行和调试其他 Java 程序一样运行和调试 Spring Boot 应用程序。

虽然您可以手动复制 Spring Boot 的 jar 文件，但我们通常建议您使用支持依赖管理的构建工具（例如 Maven 或 Gradle）。

## Maven
Spring Boot 兼容 Apache Maven 3.6.3 或更高版本。如果您尚未安装 Maven，可以按照 maven.apache.org 上的说明进行安装。

Spring Boot 的依赖项使用 `org.springframework.boot` 作为 group id。通常，您的 Maven POM 文件会继承自 `spring-boot-starter-parent` 项目，并声明对一个或多个启动器的依赖。Spring Boot 还提供了一个可选的 Maven 插件，用于创建可执行的 JAR 文件。

有关如何开始使用 Spring Boot 和 Maven 的更多详细信息，请参阅 Maven 插件参考指南中的[“入门”](https://docs.spring.io/spring-boot/maven-plugin/getting-started.html)部分。

## Gradle 

Spring Boot 兼容 Gradle 8.x（8.14 或更高版本）或 9.x。如果您尚未安装 Gradle，可以按照 gradle.org 上的说明进行操作。

Spring Boot 依赖项可以通过使用 org.springframework.boot 组来声明。通常，您的项目会声明对一个或多个 starter 的依赖。Spring Boot 提供了一个有用的 Gradle 插件，可用于简化依赖项声明并创建可执行的 jar 文件。

