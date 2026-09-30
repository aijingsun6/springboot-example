# Externalized Configuration

Spring Boot 允许你将配置外部化，以便在不同的环境中使用相同的应用程序代码。你可以使用多种外部配置源，包括 Java 属性文件、YAML 文件、环境变量和命令行参数。 
属性值可以通过 `@Value` 注解直接注入到你的 bean 中，也可以通过 Spring 的 `Environment` 抽象访问，或者通过 `@ConfigurationProperties` 绑定到结构化对象上。 
Spring Boot 使用一种非常特殊的 `PropertySource` 顺序，这种顺序的设计目的是为了实现合理的值覆盖。后续的 `PropertySource` 可以覆盖之前定义的值。


资源按以下顺序被考虑：

1. 默认属性（通过设置 `SpringApplication.setDefaultProperties(Map)` 指定）。
2. `@Configuration` 类上的 `@PropertySource` 注解。请注意，这些属性源直到应用程序上下文刷新时才会添加到环境中。这太晚了，无法配置某些属性（例如 `logging.*` 和 `spring.main.*`），因为这些属性在刷新开始之前就已经被读取了。
3. 配置数据（例如 `application.properties` 文件）。
4. 一个 `RandomValuePropertySource`，其属性仅包含在 `random.*` 中。
5. 操作系统环境变量。
6. Java 系统属性（`System.getProperties()`）。
7. 来自 `java:comp/env` 的 JNDI 属性。
8. `ServletContext` 的初始化参数。
9. `ServletConfig` 的初始化参数。
10. 来自 `SPRING_APPLICATION_JSON` 的属性（嵌入在环境变量或系统属性中的内联 JSON）。
11. 命令行参数。
12. 测试中的 `properties` 属性。可用于 `@SpringBootTest` 以及用于测试应用程序特定部分的测试注解。
13. 测试中的 `@DynamicPropertySource` 注解。
14. 测试中的 `@TestPropertySource` 注解。
15. 当 devtools 处于活动状态时，`$HOME/.config/spring-boot` 目录中的 devtools 全局设置属性。


配置数据文件的加载顺序如下：
1. 打包在您的jar文件内的应用属性文件（application.properties及其YAML变体）。
2. 打包在您的jar文件内的特定配置文件的属性文件（application-{profile}.properties及其YAML变体）。
3. 打包在您的jar文件外的应用属性文件（application.properties及其YAML变体）。
4. 打包在您的jar文件外的特定配置文件的属性文件（application-{profile}.properties及其YAML变体）。


**建议在整个应用程序中统一使用一种格式。如果在同一位置同时存在.properties和YAML格式的配置文件，则.properties文件优先。**

如果您使用环境变量而不是系统属性，大多数操作系统不允许使用点分隔的键名，但您可以使用下划线代替（例如，使用 SPRING_CONFIG_NAME 而不是 spring.config.name）。详情请参阅《从环境变量绑定》。

如果你的应用程序运行在 Servlet 容器或应用服务器中，那么可以使用 JNDI 属性（位于 java:comp/env 中）或 Servlet 上下文初始化参数来替代环境变量或系统属性，或者同时使用它们。 
为了提供一个具体的示例，假设你开发了一个使用名称属性的 @Component，如下例所示：

```java

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MyBean {

	@Value("${name}")
	private String name;

	// ...

}
```

在您的应用程序类路径中（例如，位于您的 JAR 文件内部），您可以包含一个 `application.properties` 文件，该文件为 `name` 提供一个合理的默认属性值。当在新的环境中运行时，可以在 JAR 文件外部提供一个 `application.properties` 文件来覆盖 `name` 的值。对于一次性测试，您可以通过特定的命令行参数启动（例如，`java -jar app.jar --name="Spring"`）。

`env` 和 `configprops` 端点在确定某个属性为何具有特定值时非常有用。您可以使用这两个端点来诊断意外的属性值。详情请参阅“生产就绪特性”部分。


# 1. 使用命令行属性(Accessing Command Line Properties)
默认情况下，SpringApplication 会将任何命令行选项参数（即以 -- 开头的参数，例如 --server.port=9000）转换为属性，并将其添加到 Spring 环境中。如前所述，命令行属性始终优先于基于文件的属性源。

如果您不希望将命令行属性添加到环境中，可以通过调用 `SpringApplication.setAddCommandLineProperties(false)` 来禁用此功能。


```java
java -jar foo.jar --foo=bar

@Component
public class SimpleProp {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProp.class);

    @Value("${foo}")
    private String foo;

    @PostConstruct
    public void init(){
        LOGGER.info("foo = {}", foo); // foo = bar
    }
}

```

# 2. JSON Application Properties
环境变量和系统属性通常会有一些限制，这意味着某些属性名称无法使用。为了解决这个问题，Spring Boot 允许你将一组属性编码为一个单独的 JSON 结构。 
当你的应用程序启动时，任何 `spring.application.json` 或 `SPRING_APPLICATION_JSON` 属性都会被解析并添加到环境中。 
例如，可以在 UN*X shell 的命令行中将 `SPRING_APPLICATION_JSON` 属性作为环境变量提供：

```java
$ SPRING_APPLICATION_JSON='{"my":{"name":"test"}}' java -jar myapp.jar
```

```java
export SPRING_APPLICATION_JSON='{"my":{"name":"test"}}'
java -jar myapp.jar
```


在前面的示例中，最终会在 Spring 环境中得到 my.name=test。
同样的 JSON 也可以作为系统属性提供：

```java
$ java -Dspring.application.json='{"my":{"name":"test"}}' -jar myapp.jar
```

或者你也可以通过命令行参数来提供 JSON：
```java

$ java -jar myapp.jar --spring.application.json='{"my":{"name":"test"}}'
```

如果您部署到经典的应用服务器，也可以使用名为 `java:comp/env/spring.application.json` 的 JNDI 变量。 
尽管 JSON 中的空值会被添加到生成的属性源中，但 `PropertySourcesPropertyResolver` 会将空属性视为缺失值。这意味着 JSON 不能使用空值覆盖来自较低优先级属性源的属性。

# 3. External Application Properties

Spring Boot 在应用程序启动时会自动从以下位置查找并加载 `application.properties` 和 `application.yaml` 文件：

从类路径(classpath)中查找 
- 类路径的根目录 
- 类路径的 `/config` 包 

从当前目录中查找 
- 当前目录 
- 当前目录下的 `config/` 子目录 
- `config/` 子目录的直接子目录 

该列表按优先级排序（后列出的项会覆盖先列出的项）。从加载的文件中提取的配置文档会作为 `PropertySource` 实例添加到 Spring 环境中。 

如果您不喜欢使用 `application` 作为配置文件名称，可以通过指定 `spring.config.name` 环境属性来切换到其他文件名。例如，要查找 `myproject.properties` 和 `myproject.yaml` 文件，可以按如下方式运行应用程序：

```java
$ java -jar myproject.jar --spring.config.name=myproject
```

您也可以通过使用 `spring.config.location` 环境属性来引用一个明确的位置。该属性接受一个以逗号分隔的一个或多个位置列表进行检查。 
以下示例展示了如何指定两个不同的文件：


```java

$ java -jar myproject.jar --spring.config.location=\
	optional:classpath:/default.properties,\
	optional:classpath:/override.properties
```

**使用前缀 optional：如果这些位置是可选的，并且你不介意它们不存在。**
`spring.config.name`、`spring.config.location` 和 `spring.config.additional-location` 在很早阶段就被用来确定需要加载哪些文件。它们必须被定义为环境属性（通常是操作系统环境变量、系统属性或命令行参数）。

如果 `spring.config.location` 包含目录（而非文件），这些目录应以 `/` 结尾。在运行时，它们会在加载前附加由 `spring.config.name` 生成的名称。在 `spring.config.location` 中指定的文件将直接导入。

目录和文件位置的值也会被扩展，以检查是否存在特定配置文件。例如，如果您有一个 `spring.config.location` 的值为 `classpath:myconfig.properties`，系统也会加载相应的 `classpath:myconfig-<profile>.properties` 文件。


在大多数情况下，您添加的每个 `spring.config.location` 项都会引用一个单独的文件或目录。位置是按照定义的顺序处理的，后面的位置可以覆盖前面位置的值。

如果您有复杂的位置设置，并且使用了特定于配置文件的配置文件，您可能需要提供进一步的提示，以便 Spring Boot 知道如何将它们分组。位置组（location group）是一组被视作处于同一层级的位置集合。例如，您可能希望将所有类路径位置归为一组，然后再将所有外部位置归为一组。位置组中的各项应使用 `;` 分隔。有关更多详细信息，请参阅“特定配置文件”部分中的示例。

通过 `spring.config.location` 配置的位置会替换默认位置。例如，如果将 `spring.config.location` 配置为 `optional:classpath:/custom-config/,optional:file:./custom-config/`，则考虑的全部位置集合为：

optional:classpath:custom-config/
optional:file:./custom-config/

如果您更倾向于添加额外位置而不是替换它们，可以使用 `spring.config.additional-location`。从额外位置加载的属性可以覆盖默认位置中的属性。例如，如果将 `spring.config.additional-location` 配置为 `optional:classpath:/custom-config/,optional:file:./custom-config/`，则考虑的全部位置集合为：

optional:classpath:/;optional:classpath:/config/
optional:file:./;optional:file:./config/;optional:file:./config/*/
optional:classpath:custom-config/
optional:file:./custom-config/

这种搜索顺序允许您在某个配置文件中指定默认值，然后在另一个配置文件中选择性覆盖这些值。您可以在默认位置之一中的 `application.properties`（或通过 `spring.config.name` 指定的其他基础名称）中为应用程序提供默认值。然后，这些默认值可以在运行时通过位于自定义位置之一中的另一个文件进行覆盖。

## 3.1 可选位置(Optional Locations)
默认情况下，当指定的配置数据位置不存在时，Spring Boot 会抛出 `ConfigDataLocationNotFoundException`，并且您的应用程序将无法启动。
如果您希望指定一个位置，但不介意该位置并非始终存在，可以使用 `optional:` 前缀。您可以将此前缀用于 `spring.config.location` 和 `spring.config.additional-location` 属性，以及 `spring.config.import` 声明中。
例如，将 `spring.config.import` 的值设置为 `optional:file:./myconfig.properties`，即使 `myconfig.properties` 文件缺失，您的应用程序也能启动。
如果您希望忽略所有 `ConfigDataLocationNotFoundException` 错误并始终继续启动应用程序，可以使用 `spring.config.on-not-found` 属性。通过 `SpringApplication.setDefaultProperties（…）` 或系统/环境变量将该属性值设置为 `ignore`。
## 3.2 通配符位置(Wildcard Locations)
如果配置文件的路径中最后一个路径段包含 `*` 字符，则该路径被视为通配符位置。在加载配置时，通配符会被展开，以便同时检查直接子目录。在 Kubernetes 等环境中，当存在多个配置属性来源时，通配符位置特别有用。
例如，如果您有一些 Redis 配置和一些 MySQL 配置，您可能希望将这两部分配置分开保存，但同时要求它们都存在于 `application.properties` 文件中。这可能导致两个独立的 `application.properties` 文件被挂载到不同位置，例如 `/config/redis/application.properties` 和 `/config/mysql/application.properties`。在这种情况下，使用 `config/*/` 作为通配符位置，将导致这两个文件都被处理。
默认情况下，Spring Boot 会将 `config/*/` 包含在默认搜索位置中。这意味着会搜索 `/config` 目录（位于您的 jar 文件之外）的所有子目录。
您也可以自己使用 `spring.config.location` 和 `spring.config.additional-location` 属性来指定通配符位置。

通配符位置必须仅包含一个 *，并且对于目录类型的搜索位置应以 */ 结尾，对于文件类型的搜索位置应以 */<文件名> 结尾。包含通配符的位置将根据文件名的绝对路径按字母顺序排序。

通配符位置仅适用于外部目录。您不能在类路径位置中使用通配符。


## 3.3 Profile Specific Files
除了常规的应用属性文件外，Spring Boot 还会尝试使用命名约定 application-{profile} 加载特定配置文件的属性文件。例如，如果应用程序激活了一个名为 prod 的配置文件并使用 YAML 文件，那么 application.yaml 和 application-prod.yaml 都会被加载。

特定配置文件的属性文件与标准的 application.properties 从相同的路径加载，且特定配置文件始终会覆盖通用配置文件。如果指定了多个配置文件，则采用“最后生效”的策略。例如，如果通过 spring.profiles.active 属性指定了 prod,live 两个配置文件，那么 application-prod.properties 中的值可能会被 application-live.properties 中的值覆盖。

最后生效策略应用于位置组级别。`spring.config.location` 设置为 `classpath:/cfg/,classpath:/ext/` 时，其覆盖规则与 `classpath:/cfg/;classpath:/ext/` 不同。

例如，继续我们之前提到的 `prod,live` 示例，我们可能拥有以下文件：

/cfg
 application-live.properties
/ext
 application-live.properties
 application-prod.properties

当我们使用 `spring.config.location` 设置为 `classpath:/cfg/,classpath:/ext/` 时，我们会先处理所有 `/cfg` 文件，再处理所有 `/ext` 文件：

/cfg/application-live.properties
/ext/application-prod.properties
/ext/application-live.properties

而当我们使用 `classpath:/cfg/;classpath:/ext/`（以 `;` 分隔符）时，我们会将 `/cfg` 和 `/ext` 放在同一层级进行处理：

/ext/application-prod.properties
/cfg/application-live.properties
/ext/application-live.properties


环境有一组默认配置文件（默认情况下为【default】），如果没有设置活动配置文件，则使用这些默认配置文件。换句话说，如果没有显式激活任何配置文件，则系统会考虑来自application-default的属性。

属性文件只会被加载一次。如果您已经直接导入了特定配置文件的属性文件，那么它将不会被再次导入。


## 3.4 导入额外数据(Importing Additional Data)

应用程序属性可以通过 spring.config.import 属性从其他位置导入进一步的配置数据。导入操作在发现时即被处理，并被视为插入到声明导入的文档正下方的额外文档。

例如，你可以在类路径的 application.properties 文件中包含以下内容：

```java
spring.application.name=myapp
spring.config.import=optional:file:./dev.properties
```
这将触发导入当前目录中的 dev.properties 文件（如果存在该文件）。导入的 dev.properties 中的值将优先于触发导入的文件。在上面的示例中，dev.properties 可以重新定义 `spring.application.name` 为不同的值。 
无论声明多少次，导入操作只会执行一次。 
默认情况下，属性文件使用 ISO-8859-1 字符集进行导入。要更改此设置，可以使用 `encoding` 属性：

```java
spring.config.import=classpath:import.properties[encoding=utf-8]
```

import.properties 文件现在将以 UTF-8 编码读取。
使用“固定”和“导入相对”位置
导入位置可以指定为固定位置或导入相对位置。固定位置始终解析为相同的底层资源，无论 spring.config.import 属性在何处声明。导入相对位置则相对于声明 spring.config.import 属性的文件进行解析。
以正斜杠（/）开头或具有 URL 风格前缀（如 file：、classpath: 等）的位置被视为固定位置。所有其他位置均被视为导入相对位置。
可选：在判断位置是固定还是导入相对时，前缀不会被考虑在内。
举个例子，假设我们有一个包含 application.jar 文件的 /demo 目录。我们可以添加一个 /demo/application.properties 文件，其内容如下：


```java
spring.config.import=optional:core/core.properties

```

这是一个导入的相对路径，因此如果存在，将尝试加载文件 /demo/core/core.properties。 
如果 /demo/core/core.properties 包含以下内容：

```java


spring.config.import=optional:extra/extra.properties
```

它将尝试加载 /demo/core/extra/extra.properties。其中可选的 extra/extra.properties 是相对于 /demo/core/core.properties 的路径，因此完整路径为 /demo/core/ + extra/extra.properties。

属性顺序
在 properties/yaml 文件中，单个文档内导入的定义顺序无关紧要。例如，以下两个示例会产生相同的结果：


```java

spring.config.import=my.properties
my.property=value
```

```java
my.property=value
spring.config.import=my.properties

```
在上述两个示例中，my.properties 文件中的值将优先于触发其导入的文件生效。

可以在单个 spring.config.import 键下指定多个位置。这些位置将按照定义的顺序进行处理，后导入的配置具有更高的优先级。

在适当的情况下，也会考虑导入特定配置文件的变体。上述示例将同时导入 my.properties 以及任何 my-<profile>.properties 的变体文件。

Spring Boot 提供了可插拔的 API，支持多种不同的位置地址。默认情况下，可以导入 Java 属性文件、YAML 文件和配置树。

第三方 JAR 包可以提供对其他技术的支持（文件不一定是本地文件）。例如，配置数据可以来自外部存储，如 Consul、Apache ZooKeeper 或 Netflix Archaius。

如果您希望支持自定义的位置，请参考 org.springframework.boot.context.config 包中的 ConfigDataLocationResolver 和 ConfigDataLoader 类。

## 3.5 导入无扩展名文件(Importing Extensionless Files)
某些云平台无法为挂载的卷文件添加文件扩展名。要导入这些无扩展名的文件，您需要向 Spring Boot 提示，以便它知道如何加载这些文件。您可以通过在方括号中放置扩展名提示来实现这一点。
例如，假设您有一个 /etc/config/myconfig 文件，希望将其作为 yaml 导入。您可以通过以下方式从 application.properties 中导入它：

```java

spring.config.import=file:/etc/config/myconfig[.yaml]
```
这是以下内容的简写：
```java
spring.config.import=file:/etc/config/myconfig[extension=.yaml]

```

## 3.6 文件属性(File attributes)
`spring.config.import` 配置属性支持文件属性，例如在指定编码或扩展名时可以看到。
如果需要指定多个属性，可以使用以下语法：


```java
spring.config.import=file:/etc/config/myconfig[extension=.yaml][encoding=utf-8]

```

## 3.7 使用环境变量(Using Environment Variables)

在云平台（如Kubernetes）上运行应用程序时，通常需要读取平台提供的配置值。您可以使用环境变量来实现此目的，也可以使用配置树。

您甚至可以将完整的配置以属性或YAML格式存储在（多行）环境变量中，并使用env：前缀加载它们。假设存在一个名为MY_CONFIGURATION的环境变量，其内容如下：

```java
my.name=Service1
my.cluster=Cluster1

```
使用 env: 前缀可以导入该变量中的所有属性：

```java

spring.config.import=env:MY_CONFIGURATION
```

该功能还支持指定扩展名，默认扩展名为.properties。

## 3.8 Using Configuration Trees

将配置值存储在环境变量中存在一些缺点，尤其是当该值需要保密时。作为环境变量的替代方案，许多云平台现在允许你将配置映射到挂载的数据卷中。例如，Kubernetes 可以挂载 ConfigMaps 和 Secrets 作为数据卷。 
有两种常见的卷挂载模式可以使用： 
- 一个文件包含完整的属性集（通常以 YAML 格式编写）。 
- 多个文件被写入目录树中，文件名作为“键”，文件内容作为“值”。 

对于第一种情况，你可以像上面描述的那样，直接使用 `spring.config.import` 导入 YAML 或 Properties 文件。对于第二种情况，你需要使用 `configtree:` 前缀，以便 Spring Boot 知道需要将所有文件暴露为属性。 

举个例子，假设 Kubernetes 挂载了以下卷：

```java

etc/
  config/
    myapp/
      username
      password
```
用户名文件的内容将是一个配置值，密码文件的内容将是一个机密值。 
要导入这些属性，您可以在 `application.properties` 或 `application.yaml` 文件中添加以下内容：

```java

spring.config.import=optional:configtree:/etc/config/
```

然后，您可以像往常一样从环境中访问或注入 `myapp.username` 和 `myapp.password` 属性。 
配置树下的文件夹和文件名构成了属性名称。在上面的示例中，若要以 `username` 和 `password` 的形式访问这些属性，可以将 `spring.config.import` 设置为 `optional:configtree:/etc/config/myapp`。 
使用点符号的文件名也能正确映射。例如，在上述示例中，`/etc/config` 下名为 `myapp.username` 的文件将在环境中生成一个 `myapp.username` 属性。 
配置树的值可以根据预期内容绑定到 `String` 或 `byte[]` 类型。 
如果您需要从同一父文件夹导入多个配置树，可以使用通配符快捷方式。任何以 `/*/` 结尾的 `configtree:` 位置都会将所有直接子项作为配置树导入。与非通配符导入一样，每个配置树下的文件夹和文件名构成属性名称。 
例如，给定以下卷：

```java
etc/
  config/
    dbconfig/
      db/
        username
        password
    mqconfig/
      mq/
        username
        password

```
您可以使用 configtree:/etc/config/*/ 作为导入位置：

```java
spring.config.import=optional:configtree:/etc/config/*/

```

这将添加 db.username、db.password、mq.username 和 mq.password 属性。

使用通配符加载的目录会按字母顺序排序。如果需要不同的顺序，则应将每个位置单独列为一个导入项。 
配置树也可以用于 Docker 密钥。当 Docker Swarm 服务被授予访问某个密钥的权限时，该密钥会被挂载到容器中。例如，如果名为 db.password 的密钥被挂载在 /run/secrets/ 位置，您可以通过以下方式将其提供给 Spring 环境：

```java

spring.config.import=optional:configtree:/run/secrets/
```


## 3.9 Property Placeholders

在 `application.properties` 和 `application.yaml` 中的值在使用时会通过现有的 `Environment` 进行过滤，因此可以引用之前定义的值（例如来自系统属性或环境变量）。标准的 `${name}` 属性占位符语法可以在值中的任何位置使用。属性占位符还可以通过使用冒号（：）将默认值与属性名称分隔开来，从而指定默认值，例如 `${name:default}`。

以下示例展示了带默认值和不带默认值的占位符的使用方式：
```java
app.name=MyApp
app.description=${app.name} is a Spring Boot application written by ${username:Unknown}

```
假设用户名属性未在其他地方设置，`app.description` 的值将为 "MyApp 是由 Unknown 编写的 Spring Boot 应用程序"。

在占位符中引用属性名称时，应始终使用其规范形式（仅使用小写字母的连字符形式）。这样可以让 Spring Boot 使用与 `relaxed binding @ConfigurationProperties` 相同的逻辑。

例如，`${demo.item-price}` 会从 `application.properties` 文件中获取 `demo.item-price` 和 `demo.itemPrice` 的形式，以及从系统环境变量中获取 `DEMO_ITEMPRICE`。如果你改用 `${demo.itemPrice}`，则只会从 `application.properties` 文件中获取 `demo.itemPrice` 的形式，以及从系统环境变量中获取 `DEMO_ITEMPRICE`，而 `demo.item-price` 将不会被考虑。

你也可以使用这种技术来创建现有 Spring Boot 属性的“简短”变体。详情请参阅“使用指南”中的“使用‘简短’命令行参数”部分


## 3.10 Working With Multi-Document Files
Spring Boot 允许你将单个物理文件拆分为多个逻辑文档，每个文档可独立添加。文档按从上到下的顺序处理，后续文档可以覆盖先前文档中定义的属性。

对于 application.yaml 文件，使用标准的 YAML 多文档语法。三个连续的连字符表示一个文档的结束，以及下一个文档的开始。

例如，以下文件包含两个逻辑文档：

```java

spring:
  application:
    name: "MyApp"
---
spring:
  application:
    name: "MyCloudApp"
  config:
    activate:
      on-cloud-platform: "kubernetes"
```

对于 application.properties 文件，使用特殊的 #--- 或!--- 注释来标记文档的分隔：


```java
spring.application.name=MyApp
#---
spring.application.name=MyCloudApp
spring.config.activate.on-cloud-platform=kubernetes
```

属性文件分隔符不得包含任何前导空格，且必须精确包含三个连字符（-）。分隔符前后紧邻的行不得使用相同的注释前缀。 

多文档属性文件通常与激活属性（如 spring.config.activate.on-profile）配合使用。详情请参见下一节。 

多文档属性文件无法通过 @PropertySource 或 @TestPropertySource 注解加载。


## 3.11 Activation Properties

有时仅在满足特定条件时激活一组给定的属性会很有用。例如，您可能有一些属性仅在特定配置文件处于活动状态时才相关。

您可以使用 spring.config.activate.* 来有条件地激活属性文档。

以下激活属性可用：

表 1. 激活属性

|属性|	说明|
| --- | --- |
|on-profile|	一个配置文件表达式，必须匹配才能使文档处于活动状态；或一个配置文件表达式列表，其中至少有一个必须匹配才能使文档处于活动状态。|
|on-cloud-platform|	必须检测到的 CloudPlatform，文档才能处于活动状态。|

例如，以下内容指定第二个文档仅在 Kubernetes 上运行时才处于活动状态，并且仅在 "prod" 或 "staging" 配置文件处于活动状态时才激活：
```java

myprop=always-set
#---
spring.config.activate.on-cloud-platform=kubernetes
spring.config.activate.on-profile=prod | staging
myotherprop=sometimes-set
```

# 4. 加密属性(Encrypting Properties)

Spring Boot 并未提供任何内置支持来加密属性值，但它确实提供了必要的钩子点，以便修改 Spring Environment 中包含的值。EnvironmentPostProcessor 接口允许您在应用程序启动之前操作 Environment。详情请参见《在应用程序启动前自定义 Environment 或 ApplicationContext》。

如果您需要一种安全的方式来存储凭据和密码，Spring Cloud Vault 项目提供了将外部化配置存储在 HashiCorp Vault 中的支持。


# 5. 使用 YAML
YAML 是 JSON 的超集，因此是一种用于指定分层配置数据的便捷格式。只要您在类路径中包含 SnakeYAML 库，SpringApplication 类就会自动支持 YAML 作为属性的替代方案。

如果您使用 starter，SnakeYAML 将由 spring-boot-starter 自动提供。

## 5.1 将 YAML 映射到属性文件

YAML 文档需要从其层级格式转换为可用于 Spring Environment 的扁平结构。例如，考虑以下 YAML 文档：


```java

environments:
  dev:
    url: "https://dev.example.com"
    name: "Developer Setup"
  prod:
    url: "https://another.example.com"
    name: "My Cool App"
```

为了从环境中访问这些属性，它们将被展平如下：

```java

environments.dev.url=https://dev.example.com
environments.dev.name=Developer Setup
environments.prod.url=https://another.example.com
environments.prod.name=My Cool App
```


同样地，YAML列表也需要被展平。它们被表示为带有 [index] 解引用器的属性键。例如，考虑以下 YAML：


```java
 my:
  servers:
  - "dev.example.com"
  - "another.example.com"
```
前面的示例将转换为以下属性：
```java
my.servers[0]=dev.example.com
my.servers[1]=another.example.com

```

使用 [index] 符号的属性可以通过 Spring Boot 的 Binder 类绑定到 Java 的 List 或 Set 对象。更多详细信息请参见下面的“类型安全配置属性”部分。

YAML 文件无法通过 @PropertySource 或 @TestPropertySource 注解加载。因此，如果您需要通过这种方式加载值，必须使用属性文件。


## 5.2 直接加载YAML文件

Spring Framework 提供了两个方便的类，可以用于加载 YAML 文档。`YamlPropertiesFactoryBean` 将 YAML 加载为 Properties，而 `YamlMapFactoryBean` 则将 YAML 加载为 Map。 
如果您希望将 YAML 作为 Spring 的 PropertySource 加载，也可以使用 `YamlPropertySourceLoader` 类。

# 6. 配置随机值

RandomValuePropertySource 可用于注入随机值（例如，注入到密钥或测试用例中）。它可以生成整数、长整型、UUID 或字符串，如下例所示：

```java

my.secret=${random.value}
my.number=${random.int}
my.bignumber=${random.long}
my.uuid=${random.uuid}
my.number-less-than-ten=${random.int(10)}
my.number-in-range=${random.int[1024,65536]}
```

random.int* 语法格式为 OPEN 值 （，最大值） CLOSE，其中 OPEN 和 CLOSE 可以是任意字符，而值和最大值必须是整数。如果提供了最大值，则该值表示最小值，最大值表示最大值（不包括在内）。


# 7. 配置系统环境属性

Spring Boot 支持为环境属性设置前缀。这在系统环境被多个具有不同配置需求的 Spring Boot 应用程序共享时非常有用。可以在应用程序运行之前，通过调用 `SpringApplication` 的 `setEnvironmentPrefix（…）` 方法，直接为系统环境属性设置前缀。

例如，如果将前缀设置为 `input`，那么像 `remote.timeout` 这样的属性在系统中会被解析为 `INPUT_REMOTE_TIMEOUT`。

**该前缀仅适用于系统环境属性。在从其他来源读取属性时，上述示例将继续使用 remote.timeout。**


# 8. 类型安全的配置属性
使用 `@Value("${property}")` 注解来注入配置属性有时可能显得繁琐，尤其是在处理多个属性或数据具有层次结构时。Spring Boot 提供了一种替代方法来处理属性，这种方法允许强类型 Bean 来管理和验证应用程序的配置。

另请参阅 @Value 和类型安全配置属性之间的区别。

## 8.1 JavaBean Properties Binding

可以绑定一个声明了标准 JavaBean 属性的 bean，如下例所示：

```java

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("my.service")
public class MyProperties {

	private boolean enabled;

	private InetAddress remoteAddress;

	private final Security security = new Security();

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public InetAddress getRemoteAddress() {
		return this.remoteAddress;
	}

	public void setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public Security getSecurity() {
		return this.security;
	}

	public static class Security {

		private String username;

		private String password;

		private List<String> roles = new ArrayList<>(Collections.singleton("USER"));

		public String getUsername() {
			return this.username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return this.password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public List<String> getRoles() {
			return this.roles;
		}

		public void setRoles(List<String> roles) {
			this.roles = roles;
		}

	}

}
```
前面的POJO定义了以下属性：
- my.service.enabled，默认值为false。
- my.service.remote-address，其类型可以强制转换为String。
- my.service.security.username，包含一个嵌套的“security”对象，该对象的名称由属性的名称决定。特别地，该处完全不使用类型，本可以是SecurityProperties。
- my.service.security.password。
- my.service.security.roles，一个String集合，默认值为USER。

要在属性名称中使用保留关键字（例如 my.service.import），请在属性的字段上使用 @Name 注解。

映射到 Spring Boot 中的 @ConfigurationProperties 类的属性，可以通过属性文件、YAML 文件、环境变量和其他机制进行配置。这些属性是公共 API，但类本身的访问器（getter/setter）并不打算直接使用。

这种安排依赖于默认的空构造函数，并且通常必须包含 getter 和 setter 方法，因为绑定是通过标准的 Java Bean 属性描述符实现的，这与 Spring MVC 中的情况类似。在以下情况下，setter 方法可以省略：
预初始化的 Map 和集合（只要它们使用可变实现进行初始化，如前面示例中的 roles 字段）。
预初始化的嵌套 POJO（如前面示例中的 Security 字段）。如果您希望绑定器通过默认构造函数动态创建实例，则需要一个 setter 方法。
有些人使用 Project Lombok 来自动添加 getter 和 setter 方法。请确保 Lombok 不会为此类型生成任何特定的构造函数，因为容器会自动使用它来实例化对象。
最后，仅考虑标准的 Java Bean 属性，不支持对静态属性的绑定。


## 8.2 Constructor Binding

上一节中的示例可以以不可变的方式重写，如下所示：

```java

import java.net.InetAddress;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("my.service")
public class MyProperties {

	private final boolean enabled;

	private final InetAddress remoteAddress;

	private final Security security;


	public MyProperties(boolean enabled, InetAddress remoteAddress, Security security) {
		this.enabled = enabled;
		this.remoteAddress = remoteAddress;
		this.security = security;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public InetAddress getRemoteAddress() {
		return this.remoteAddress;
	}

	public Security getSecurity() {
		return this.security;
	}

	public static class Security {

		private final String username;

		private final String password;

		private final List<String> roles;


		public Security(String username, String password, @DefaultValue("USER") List<String> roles) {
			this.username = username;
			this.password = password;
			this.roles = roles;
		}

		public String getUsername() {
			return this.username;
		}

		public String getPassword() {
			return this.password;
		}

		public List<String> getRoles() {
			return this.roles;
		}

	}

}
```

在此配置中，存在单个参数化构造函数意味着应使用构造函数绑定。这意味着绑定器将找到一个具有您希望绑定的参数的构造函数。如果您的类有多个构造函数，可以使用 @ConstructorBinding 注解来指定用于构造函数绑定的构造函数。
要为某个类禁用构造函数绑定，必须使用 @Autowired 注解标记参数化构造函数，或将其设为私有。Kotlin 开发者可以使用空的主构造函数来禁用构造函数绑定。
例如：

```java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("my")
public class MyProperties {

	final MyBean myBean;

	private String name;


	@Autowired
	public MyProperties(MyBean myBean) {
		this.myBean = myBean;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
```

1. 记录类（records）可以使用构造器绑定。除非你的记录类有多个构造器，否则不需要使用 @ConstructorBinding。
2. 构造器绑定类的嵌套成员（如上例中的 Security）也会通过其构造器进行绑定。
3. 要使用构造器绑定，必须通过 @EnableConfigurationProperties 或配置属性扫描来启用该类。你不能对通过常规 Spring 机制创建的 bean（例如 @Component bean、通过 @Bean 方法创建的 bean 或通过 @Import 加载的 bean）使用构造器绑定。
4. 要使用构造器绑定，该类必须使用 -parameters 参数进行编译。如果你使用 Spring Boot 的 Gradle 插件，或者使用 Maven 并继承 spring-boot-starter-parent，这将自动完成。
5. 不建议在 @ConfigurationProperties 中使用 Optional，因为它主要用作返回类型。因此，它不太适合配置属性注入。如果你确实声明了一个构造器绑定的 Optional 属性且它没有值，则会绑定一个空的 Optional。
6. 如果要在属性名称中使用保留关键字（例如 my.service.import），请在构造器参数上使用 @Name 注解。

## 8.3 @DefaultValue and Binding

默认值可以通过在构造函数参数和记录组件上使用 `@DefaultValue` 来指定。转换服务将应用于将注解的字符串值强制转换为缺失属性的目标类型。 
在上面的 `MyProperties` 示例中，您可以看到嵌套的 `Security` 类为 `roles` 参数使用了 `@DefaultValue("USER")`。这意味着如果定义了安全属性，但未定义 `roles`，则将绑定默认值 `"USER"`。 
例如，以下属性：

```java

my.service.enabled=true
my.service.security.username=admin
```

将被绑定为新的 MyProperties(true, null, new Security("admin", null, List.of("USER"))) 
如果安全属性完全不存在，则 Security 实例将为 null。 
例如，以下属性：

```java
my.service.enabled=true

```

将被绑定为新的 MyProperties(true, null, null)。



如果你希望使用完全默认的 Security 实例触发绑定，可以定义一个空的安全属性。 
对于 YAML，可以使用以下语法： 
```java
my: 
 service: 
 enabled: true 
 security: {} 
```

对于 Properties，可以使用： 
```java
my.service.enabled=true 
my.service.security=

```
 
将被绑定为新的 MyProperties(true, null, new Security(null, null, List.of("USER")))

如果你想始终绑定一个非空的 Security 实例，即使在属性缺失的情况下，也可以使用空的 @DefaultValue 注解：

```java

	public MyProperties(boolean enabled, InetAddress remoteAddress, @DefaultValue Security security) {
		this.enabled = enabled;
		this.remoteAddress = remoteAddress;
		this.security = security;
	}
```

在使用 Kotlin 时，您需要将用户名和密码参数声明为可为空，因为它们没有默认值。


## 8.4 Default Values

在配置属性中定义的默认值不会反映在环境中。在上述示例中，绑定到 `my.service` 的 `MyProperties` 的 `enabled` 属性默认值为 `false`。然而，如果用户未设置该属性，环境中也不会存在 `my.service.enabled` 且其值为 `false`。具体来说，这会阻止您在配置属性中使用 `@Value("${my.service.enabled}")` 或 `my.service.enabled` 作为占位符，除非显式提供了默认值。如果您需要在环境中查询该属性（例如在 `Condition` 实现中），同样也需要提供默认值。


## 8.5 Enabling @ConfigurationProperties-annotated Types
Spring Boot 提供了基础设施来绑定 `@ConfigurationProperties` 类型并将其注册为 Bean。你可以逐个类地启用配置属性，或者启用配置属性扫描，其工作方式与组件扫描类似。

有时，带有 `@ConfigurationProperties` 注解的类可能不适合进行扫描，例如，如果你正在开发自己的自动配置，或者希望有条件地启用它们。在这些情况下，可以使用 `@EnableConfigurationProperties` 注解来指定要处理的类型列表。这可以在任何 `@Configuration` 类上完成，如下例所示：

```java

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SomeProperties.class)
public class MyConfiguration {



}
```

```java
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("some.properties")
public class SomeProperties {

}
```


要使用配置属性扫描功能，请在应用程序中添加 @ConfigurationPropertiesScan 注解。通常，该注解会被添加到带有 @SpringBootApplication 注解的主应用程序类上，但也可以添加到任何 @Configuration 类上。默认情况下，扫描将从声明该注解的类所在的包开始。如果您希望指定要扫描的特定包，可以按照以下示例进行操作：

```java

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan({ "com.example.app", "com.example.another" })
public class MyApplication {

}
```

我们建议@ConfigurationProperties仅处理环境相关的内容，特别是不要从上下文中注入其他Bean。对于难例，可以使用setter注入或框架提供的任何*Aware接口（例如，如果需要访问Environment，可以使用EnvironmentAware）。如果仍想通过构造函数注入其他Bean，则配置属性Bean必须使用@Component注解，并采用基于JavaBean的属性绑定方式。

## 8.6 Using @ConfigurationProperties-annotated Types

这种配置方式与SpringApplication的外部YAML配置配合使用效果尤佳，如下所示：

```java

my:
  service:
    remote-address: 192.168.1.1
    security:
      username: "admin"
      roles:
      - "USER"
      - "ADMIN"
```

要使用 @ConfigurationProperties bean，您可以像注入其他任何 bean 一样注入它们，如下例所示：

```java

import org.springframework.stereotype.Service;

@Service
public class MyService {

	private final MyProperties properties;

	public MyService(MyProperties properties) {
		this.properties = properties;
	}

	public void openConnection() {
		Server server = new Server(this.properties.getRemoteAddress());
		server.start();
		// ...
	}

	// ...

}
```





# 参考文档
- [Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)