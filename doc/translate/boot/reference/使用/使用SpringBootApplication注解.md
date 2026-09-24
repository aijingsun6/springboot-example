# 使用 SpringBootApplication 注解(Using the @SpringBootApplication Annotation)

许多 Spring Boot 开发者希望他们的应用程序能够使用自动配置、组件扫描，并能够在“应用程序类”上定义额外的配置。一个 @SpringBootApplication 注解就可以启用这三个特性，具体如下：

@EnableAutoConfiguration：启用 Spring Boot 的自动配置机制 
@ComponentScan：在应用程序所在的包中启用 @Component 扫描（参见最佳实践） 
@SpringBootConfiguration：允许在上下文中注册额外的 Bean 或导入其他配置类。这是 Spring 标准 @Configuration 的替代方案，有助于在集成测试中检测配置。


```java

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Same as @SpringBootConfiguration @EnableAutoConfiguration @ComponentScan
@SpringBootApplication
public class MyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}

}
```

@SpringBootApplication 还提供了别名，用于自定义 @EnableAutoConfiguration 和 @ComponentScan 的属性。这些功能都不是强制性的，您可以选择用它们所启用的任何功能来替换这个单一注解。例如，您可能不希望在应用程序中使用组件扫描或配置属性扫描：

```java


import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import({ SomeConfiguration.class, AnotherConfiguration.class })
public class MyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}

}
```

在此示例中，MyApplication 与任何其他 Spring Boot 应用程序类似，只是它不会自动检测带有 @Component 注解的类和带有 @ConfigurationProperties 注解的类，并且用户定义的 bean 是显式导入的（参见 @Import）。


