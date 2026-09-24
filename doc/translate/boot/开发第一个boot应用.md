# 开发第一个boot应用(Developing Your First Spring Boot Application)

本节介绍如何开发一个小的“Hello World!” Web 应用程序，以突出展示 Spring Boot 的一些关键特性。您可以选择 Maven 或 Gradle 作为构建工具。

spring.io 网站包含许多使用 Spring Boot 的“入门指南”。如果您需要解决某个具体问题，可以先查看该网站。

您可以通过访问 start.spring.io 并从依赖项搜索器中选择“Web”启动器来跳过以下步骤。这样做会生成一个新的项目结构，以便您可以立即开始编码。更多详情请参阅 start.spring.io 的用户指南。

本教程提供了构建第一个 Spring Boot 应用程序的步骤。您不需要了解 Java，但如果您是喜欢先学习基础知识的人，那么在开始之前，您可能想先查看 [dev.java/learn](https://dev.java/learn/)。


# 1.前置条件

在开始之前，请打开一个终端并运行以下命令，以确保您已安装有效版本的 Java：

```java
$ java -version
openjdk version "17.0.18" 2026-01-20 LTS
OpenJDK Runtime Environment (build 17.0.18+10-LTS)
OpenJDK 64-Bit Server VM (build 17.0.18+10-LTS, mixed mode, sharing)
```

此示例需要在其自己的目录中创建。后续说明假设您已创建了一个合适的目录，并且该目录是您当前所在的目录。

## 1.1 Maven

如果你想使用 Maven，请确保已安装 Maven：

```java

$ mvn -v
Apache Maven 3.9.12 (848fbb4bf2d427b72bdb2471c22fced7ebd9a7a1)
Maven home: /Users/developer/.sdkman/candidates/maven/current
Java version: 17.0.18, vendor: BellSoft, runtime: /Users/developer/.sdkman/candidates/java/17.0.18-librca
```

## 1.2 Gradle

如果你想使用 Gradle，请确保已安装 Gradle：

```java

$ gradle --version

------------------------------------------------------------
Gradle 8.14.4
------------------------------------------------------------

Build time:    2026-01-23 16:30:23 UTC
Revision:      ad5ff774b4b0e9a8a0cf1a14ca70d7230003c3ad

Kotlin:        2.0.21
Groovy:        3.0.25
Ant:           Apache Ant(TM) version 1.10.15 compiled on August 25 2024
Launcher JVM:  17.0.18 (BellSoft 17.0.18+10-LTS)
Daemon JVM:    /Users/developer/.sdkman/candidates/java/17.0.18-librca (no JDK specified, using current Java home)
OS:            Mac OS X 26.3 aarch64
```

# 2. 使用 Maven 设置项目环境
我们需要首先创建一个 Maven 的 pom.xml 文件。pom.xml 是用于构建项目的配方。打开你常用的文本编辑器并添加以下内容：

```java

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
    <parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>4.1.1</version>
	</parent>
	<groupId>com.example</groupId>
	<artifactId>myproject</artifactId>
	<version>0.0.1-SNAPSHOT</version>

	<!-- Additional lines to be added here... -->

</project>
```
前面的清单应该能为你提供一个可用的构建。你可以通过运行 `mvn package` 来测试它（目前可以忽略“jar 将为空——没有标记内容包含在内！”的警告）。此时，你可以将项目导入一个 IDE（大多数现代 Java IDE 都内置了对 Maven 的支持）。为了简单起见，在本示例中我们继续使用纯文本编辑器。


# 3. 使用 Gradle 设置项目环境
我们需要首先创建一个Gradle的build.gradle文件。build.gradle是用于构建项目的构建脚本。打开您喜欢的文本编辑器并添加以下内容：

```java

plugins {
	id 'java'
	id 'org.springframework.boot' version '4.1.1'
	id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
}
```

前面的清单应该能为你提供一个可用的构建。你可以通过运行 `gradle classes` 来测试它。 
此时，你可以将项目导入一个 IDE（大多数现代 Java IDE 都内置了对 Gradle 的支持）。为了简单起见，在本示例中我们继续使用纯文本编辑器。

# 4.添加类路径依赖项

Spring Boot 提供了多个启动器（starters），允许你将 JAR 包添加到类路径中。这些启动器提供了在开发特定类型应用程序时可能需要的依赖项。

## 4.1 Maven
大多数 Spring Boot 应用程序在 POM 的父元素部分都会使用 `spring-boot-starter-parent`。`spring-boot-starter-parent` 是一个特殊的启动器，它提供了有用的 Maven 默认配置。它还包含一个依赖管理部分，因此你可以省略“受认可”依赖项的版本标签。

由于我们正在开发一个 Web 应用程序，因此需要添加 `spring-boot-starter-webmvc` 依赖。在此之前，我们可以通过运行以下命令来查看当前已有的内容：

```bash

$ mvn dependency:tree

[INFO] com.example:myproject:jar:0.0.1-SNAPSHOT
```

`mvn dependency:tree` 命令会打印出项目依赖关系的树状表示。你可以看到，`spring-boot-starter-parent` 本身并不提供任何依赖项。要添加必要的依赖项，可以编辑你的 `pom.xml` 文件，并在 `parent` 部分下方立即添加 `spring-boot-starter-webmvc` 依赖项：

```xml
<dependencies>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-webmvc</artifactId>
	</dependency>
</dependencies>
```
如果你再次运行 `mvn dependency:tree`，你会发现现在多了一些额外的依赖项，包括 Tomcat Web 服务器和 Spring Boot 本身。

## 4.2 Gradle

大多数 Spring Boot 应用程序使用 org.springframework.boot Gradle 插件。该插件提供了有用的默认配置和 Gradle 任务。io.spring.dependency-management Gradle 插件提供了依赖管理功能，使您可以省略“受支持”依赖项的版本标签。

由于我们正在开发一个 Web 应用程序，因此添加了 spring-boot-starter-webmvc 依赖项。在此之前，我们可以通过运行以下命令查看当前已有的内容：

```bash
$ gradle dependencies

> Task :dependencies

------------------------------------------------------------
Root project 'myproject'
------------------------------------------------------------
```

gradle dependencies 命令会打印出项目依赖关系的树状结构。目前，该项目没有任何依赖项。要添加必要的依赖项，请编辑你的 build.gradle 文件，并在 dependencies 部分添加 spring-boot-starter-webmvc 依赖项：

```bash

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-webmvc'
}
```
如果你再次运行 Gradle 依赖项，你会发现现在多了一些额外的依赖项，包括 Tomcat Web 服务器和 Spring Boot 本身。


# 5. 编写代码

要完成我们的应用程序，我们需要创建一个 Java 文件。默认情况下，Maven 和 Gradle 会从 `src/main/java` 目录编译源代码，因此你需要创建该目录结构，然后添加一个名为 `src/main/java/com/example/MyApplication.java` 的文件，其中包含以下代码：

```java

package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class MyApplication {

	@RequestMapping("/")
	String home() {
		return "Hello World!";
	}

	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}

}
```
尽管这里代码不多，但实际发生的事情却不少。我们将在接下来的几个小节中逐步讲解其中的重要部分。

@RestController 和 @RequestMapping 注解

MyApplication 类上的第一个注解是 @RestController。这被称为“样板注解”（stereotype annotation）。它为阅读代码的人以及 Spring 提供了提示，表明该类扮演着特定的角色。在本例中，我们的类是一个 Web @Controller，因此 Spring 在处理传入的 Web 请求时会考虑它。

@RequestMapping 注解提供了“路由”信息。它告诉 Spring，任何以 / 路径开头的 HTTP 请求都应映射到 home 方法。@RestController 注解则告诉 Spring 将生成的字符串直接返回给调用者。

@RestController 和 @RequestMapping 注解是 Spring MVC 的注解（它们并非 Spring Boot 特有）。更多细节请参见 Spring 参考文档中的 MVC 部分。

@SpringBootApplication 注解

第二个类级注解是 @SpringBootApplication。这个注解被称为“元注解”（meta-annotation），它结合了 @SpringBootConfiguration、@EnableAutoConfiguration 和 @ComponentScan。

其中，我们在这里最感兴趣的注解是 @EnableAutoConfiguration。@EnableAutoConfiguration 告诉 Spring Boot 根据你添加的 jar 依赖项来“猜测”你希望如何配置 Spring。由于 spring-boot-starter-webmvc 添加了 Tomcat 和 Spring MVC，自动配置会假设你正在开发一个 Web 应用程序，并相应地设置 Spring。

Starter 和自动配置

自动配置的设计初衷是与 Starter 配合良好，但这两个概念并非直接绑定。你可以自由地在 Starter 之外选择和添加 jar 依赖项。Spring Boot 仍然会尽力为你的应用程序进行自动配置。

“main”方法

我们应用程序的最后一部分是 main 方法。这是一个遵循 Java 应用程序入口点惯例的标准方法。我们的 main 方法通过调用 run 方法，将控制权委托给 Spring Boot 的 SpringApplication 类。SpringApplication 会启动我们的应用程序，启动 Spring，而 Spring 又会启动自动配置的 Tomcat Web 服务器。我们需要将 MyApplication.class 作为参数传递给 run 方法，以告诉 SpringApplication 哪个是主要的 Spring 组件。args 数组也会被传递，以暴露任何命令行参数。


# 6. 运行

## 6.1 Maven

此时，你的应用程序应该可以正常运行了。由于你使用了 `spring-boot-starter-parent` 的 POM 文件，你拥有一个有用的运行目标，可以用来启动应用程序。在项目根目录下输入 `mvn spring-boot:run` 即可启动应用程序。你应该会看到类似以下的输出：

```bash
$ mvn spring-boot:run

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v4.1.1)
....... . . .
....... . . . (log output here)
....... . . .
........ Started MyApplication in 0.906 seconds (process running for 6.514)


```

如果你打开网页浏览器访问 localhost:8080，你应该会看到以下输出：
Hello World!
要优雅地退出应用程序，请按 ctrl-c。


## 6.2 Gradle

此时，你的应用程序应该可以正常运行了。由于你使用了 org.springframework.boot Gradle 插件，因此你拥有一个非常有用的 bootRun 任务，可以用来启动应用程序。在项目根目录下输入 gradle bootRun 来启动应用程序。你应该会看到类似以下的输出：

```bash
$ gradle bootRun

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v4.1.1)
....... . . .
....... . . . (log output here)
....... . . .
........ Started MyApplication in 0.906 seconds (process running for 6.514)


```

# 7. 创建可执行的Jar文件

我们通过创建一个完全独立的可执行 JAR 文件来结束这个示例，该文件可以在生产环境中运行。可执行 JAR 文件（有时称为“uber JAR”或“fat JAR”）是一种归档文件，其中包含你编译的类以及代码运行所需的所有 JAR 依赖项。

可执行 JAR 文件与 Java

Java 并没有提供一种标准的方式来加载嵌套的 JAR 文件（即本身包含在另一个 JAR 文件中的 JAR 文件）。如果你希望分发一个完全独立的应用程序，这可能会带来问题。

为了解决这个问题，许多开发者使用“uber JAR”。uber JAR 会将应用程序所有依赖项中的所有类打包到一个单独的归档文件中。这种方法的问题是，很难看出你的应用程序中到底包含了哪些库。此外，如果多个 JAR 文件中使用了相同的文件名（但内容不同），也可能引发问题。

Spring Boot 采取了不同的方法，它允许你直接将 JAR 文件嵌套在一起。



## 7.1 Maven

要创建一个可执行的 JAR 文件，我们需要在 pom.xml 中添加 spring-boot-maven-plugin。为此，请在 dependencies 部分下方插入以下行：

```java

<build>
	<plugins>
		<plugin>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-maven-plugin</artifactId>
		</plugin>
	</plugins>
</build>
```

spring-boot-starter-parent 的 POM 文件包含了用于绑定 repackaging 目标的 <executions> 配置。如果您不使用父 POM，则需要自行声明此配置。详情请参见插件文档。 
保存您的 pom.xml 文件，并从命令行运行 `mvn package`，如下所示：

```bash

$ mvn package

[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building myproject 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] .... ..
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ myproject ---
[INFO] Building jar: /Users/developer/example/spring-boot-example/target/myproject-0.0.1-SNAPSHOT.jar
[INFO]
[INFO] --- spring-boot-maven-plugin:4.1.1:repackage (default) @ myproject ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

如果你查看目标目录，应该会看到 `myproject-0.0.1-SNAPSHOT.jar`。该文件的大小大约为 18 MB。如果你想查看文件内部内容，可以使用 `jar tvf` 命令，如下所示：
```
$ jar tvf target/myproject-0.0.1-SNAPSHOT.jar
```
你还会在目标目录中看到一个名为 `myproject-0.0.1-SNAPSHOT.jar.original` 的文件，它的大小要小得多。这是 Maven 在 Spring Boot 重新打包之前创建的原始 jar 文件。

要运行该应用程序，可以使用 `java -jar` 命令，如下所示：


```java

$ java -jar target/myproject-0.0.1-SNAPSHOT.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v4.1.1)
....... . . .
....... . . . (log output here)
....... . . .
........ Started MyApplication in 0.497 seconds (process running for 0.651)
```


## 7.2 Gradle

```bash

$ gradle bootJar

BUILD SUCCESSFUL in 639ms
3 actionable tasks: 3 executed
```
如果你查看 build/libs 目录，应该会看到 myproject-0.0.1-SNAPSHOT.jar 文件。该文件的大小大约为 18 MB。如果你想查看文件内部内容，可以使用 jar tvf 命令，如下所示：
$ jar tvf build/libs/myproject-0.0.1-SNAPSHOT.jar
要运行该应用程序，请使用 java -jar 命令，如下所示：


```bash

$ java -jar build/libs/myproject-0.0.1-SNAPSHOT.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v{version-spring-boot})
....... . . .
....... . . . (log output here)
....... . . .
........ Started MyApplication in 0.484 seconds (process running for 0.642)
```
如前所述，要退出应用程序，请按 Ctrl-C。
