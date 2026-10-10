# 通过JMX进行监控与管理(Monitoring and Management over JMX)

Java 管理扩展（JMX）提供了一种标准的机制来监控和管理应用程序。默认情况下，此功能是禁用的。您可以通过将 `spring.jmx.enabled` 配置属性设置为 `true` 来启用它。Spring Boot 会将最合适的 `MBeanServer` 暴露为一个 ID 为 `mbeanServer` 的 bean。任何使用 Spring JMX 注解（`@org.springframework.jmx.export.annotation.ManagedResource`、`@ManagedAttribute` 或 `@ManagedOperation`）标注的 bean 都会暴露给该 `MBeanServer`。

如果您的平台提供了标准的 `MBeanServer`，Spring Boot 会使用它，必要时默认使用 JVM 的 `MBeanServer`。如果所有尝试都失败，则会创建一个新的 `MBeanServer`。

`spring.jmx.enabled` 仅影响 Spring 提供的管理 bean。启用其他库（例如 Log4j2 或 Quartz）提供的管理 bean 是独立的。

更多详情请参见 `JmxAutoConfiguration` 类。

默认情况下，Spring Boot 还会将管理端点作为 JMX MBeans 暴露在 `org.springframework.boot` 域下。若要完全控制 JMX 域中的端点注册，可以考虑注册您自己的 `EndpointObjectNameFactory` 实现。

# 1. 自定义MBean名称

MBean 的名称通常是从端点的 ID 生成的。例如，健康端点会暴露为 org.springframework.boot:type=Endpoint,name=Health。 
如果你的应用程序包含多个 Spring ApplicationContext，你可能会发现名称冲突。要解决这个问题，可以将 spring.jmx.unique-names 属性设置为 true，这样 MBean 的名称将始终是唯一的。 
你还可以自定义暴露端点所使用的 JMX 域。以下设置展示了在 application.properties 中如何实现这一点的示例：

```yaml

spring:
  jmx:
    unique-names: true
management:
  endpoints:
    jmx:
      domain: "com.example.myapp"
```

# 2. 禁用 JMX 端点

如果您不希望通过 JMX 暴露端点，可以将 `management.endpoints.jmx.exposure.exclude` 属性设置为 `*`，如下例所示：


```bash
management:
  endpoints:
    jmx:
      exposure:
        exclude: "*"
```