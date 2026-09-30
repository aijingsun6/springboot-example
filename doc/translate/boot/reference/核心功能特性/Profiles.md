# Profiles

Spring Profiles 提供了一种方式，可以将应用程序配置的某些部分进行隔离，并使其仅在特定环境中可用。任何 @Component、@Configuration 或 @ConfigurationProperties 都可以使用 @Profile 注解来标记，以限制其加载的时机，如下例所示：

```java

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("production")
public class ProductionConfiguration {

	// ...

}
```

NOTE:
如果通过 `@EnableConfigurationProperties` 注解注册 `@ConfigurationProperties` Bean，而不是通过自动扫描的方式，那么需要在带有 `@EnableConfigurationProperties` 注解的 `@Configuration` 类上指定 `@Profile` 注解。而在 `@ConfigurationProperties` 被扫描的情况下，可以直接在 `@ConfigurationProperties` 类本身上指定 `@Profile` 注解。



你可以使用 `spring.profiles.active` 环境属性来指定哪些配置文件是激活的。你可以按照本章前面描述的任何一种方式来指定该属性。例如，你可以将其包含在 `application.properties` 文件中，如下例所示：


```java

spring.profiles.active=dev,hsqldb
```

你也可以通过命令行使用以下开关来指定它：```spring.profiles.active=dev,hsqldb```
如果没有激活任何配置文件，则会启用一个默认配置文件。默认配置文件的名称为 default，可以通过 spring.profiles.default 环境属性进行调整，如下例所示：
```java

spring.profiles.default=none
```


`spring.profiles.active` 和 `spring.profiles.default` 只能用于非特定配置文件（profile-specific）的文档中。这意味着它们不能包含在特定配置文件或由 `spring.config.activate.on-profile` 激活的文档中。

例如，第二个文档配置是无效的：


```java

spring.profiles.active=prod
#---
spring.config.activate.on-profile=prod
spring.profiles.active=metrics
```
spring.profiles.active 属性遵循与其他属性相同的排序规则。优先级最高的 PropertySource 将生效。这意味着你可以在 application.properties 中指定激活的配置文件，然后通过命令行开关覆盖它们。

# 1. Adding Active Profiles

有时，添加属性以增强活动配置文件而不是替换它们是有用的。spring.profiles.include 属性可用于在 spring.profiles.active 属性激活的配置文件之上添加活动配置文件。SpringApplication 入口点还具有用于设置额外配置文件的 Java API。请参阅 SpringApplication 中的 setAdditionalProfiles() 方法。

例如，当运行具有以下属性的应用程序时，即使使用 --spring.profiles.active 开关运行，common 和 local 配置文件也会被激活：

```java

spring.profiles.include[0]=common
spring.profiles.include[1]=local
```

包含的配置文件会在任何 `spring.profiles.active` 配置文件之前被添加。
`spring.profiles.include` 属性会为每个属性源进行处理，因此通常用于列表的复杂类型合并规则不适用。
与 `spring.profiles.active` 类似，`spring.profiles.include` 只能用于非特定配置文件的文档中。这意味着它不能包含在特定配置文件的文件或由 `spring.config.activate.on-profile` 激活的文档中。
下一节中描述的配置文件组也可以用来添加活动配置文件，前提是某个给定的配置文件是活动的。



# 2. Profile Groups

有时，您在应用程序中定义和使用的配置文件过于细粒度，导致使用起来变得繁琐。例如，您可能拥有 proddb 和 prodmq 配置文件，用于分别启用数据库和消息传递功能。 
为了解决这个问题，Spring Boot 允许您定义配置文件组。配置文件组允许您为一组相关的配置文件定义一个逻辑名称。 
例如，我们可以创建一个生产组，该组包含我们的 proddb 和 prodmq 配置文件。

```java
spring.profiles.group.production[0]=proddb
spring.profiles.group.production[1]=prodmq
```

我们的应用程序现在可以使用 --spring.profiles.active=production 一次性激活生产环境、生产数据库和生产消息队列配置文件。

与 `spring.profiles.active` 和 `spring.profiles.include` 类似，`spring.profiles.group` 只能用于非特定配置文件的文档中。这意味着它不能包含在特定配置文件的文件或由 `spring.config.activate.on-profile` 激活的文档中。


# 3. Programmatically Setting Profiles
您可以通过在应用程序运行之前调用 `SpringApplication.setAdditionalProfiles（…）` 来以编程方式设置活动配置文件。此外，也可以通过使用 Spring 的 `ConfigurableEnvironment` 接口来激活配置文件。

# 4. Profile-specific Configuration Files
特定配置文件（profile-specific）的 `application.properties`（或 `application.yaml`）以及通过 `@ConfigurationProperties` 引用的文件均被视为文件并会被加载。详情请参阅“配置文件特定文件”部分。



# 参考文档
- [Profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html)
- [Profile Specific Files](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.profile-specific)