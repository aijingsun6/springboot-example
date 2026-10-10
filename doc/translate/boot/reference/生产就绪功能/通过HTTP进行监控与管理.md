# 通过HTTP进行监控与管理(Monitoring and Management Over HTTP)

如果您正在开发一个Web应用程序，Spring Boot Actuator会自动配置所有启用的端点，使其通过HTTP暴露。默认约定是使用端点的ID，并在其前面加上前缀 `/actuator` 作为URL路径。例如，`health` 端点会暴露为 `/actuator/health`。

Actuator 在 Spring MVC、Spring WebFlux 和 Jersey 中都原生支持。如果同时存在 Jersey 和 Spring MVC，则会使用 Spring MVC。

为了获得 API 文档中描述的正确 JSON 响应，Jackson 是一个必需的依赖项。对于 Spring MVC 和 Spring WebFlux，应使用 Jackson 3。Jersey 目前尚无 Jackson 3 模块，因此您需要使用 Jackson 2。

# 1. 自定义管理端点路径 
有时，自定义管理端点的前缀会很有用。例如，您的应用程序可能已经将 `/actuator` 用于其他用途。您可以使用 `management.endpoints.web.base-path` 属性来更改管理端点的前缀，如下例所示：

```yaml

management:
  endpoints:
    web:
      base-path: "/manage"
```

前面的 application.properties 示例将端点从 /actuator/{id} 更改为 /manage/{id}（例如 /manage/info）。
除非管理端口已配置为通过不同的 HTTP 端口暴露端点，否则 management.endpoints.web.base-path 是相对于 server.servlet.context-path（对于 Servlet Web 应用程序）或 spring.webflux.base-path（对于响应式 Web 应用程序）的。如果配置了 management.server.port，则 management.endpoints.web.base-path 是相对于 management.server.base-path 的。
如果您希望将端点映射到不同的路径，可以使用 management.endpoints.web.path-mapping 属性。
以下示例将 /actuator/health 重新映射为 /healthcheck：

```yaml

management:
  endpoints:
    web:
      base-path: "/"
      path-mapping:
        health: "healthcheck"
```

# 2. 自定义管理服务器端口
在基于云的部署中，使用默认的HTTP端口暴露管理端点是合理的选择。然而，如果您的应用程序运行在您自己的数据中心内，您可能更倾向于使用不同的HTTP端口来暴露端点。
您可以通过设置`management.server.port`属性来更改HTTP端口，如下例所示：
```bash
management.server.port=8081
```

在 Cloud Foundry 上，默认情况下，应用程序仅通过 8080 端口接收 HTTP 和 TCP 路由的请求。如果您想在 Cloud Foundry 上使用自定义管理端口，需要显式设置应用程序的路由，以便将流量转发到该自定义端口。

#  3.配置管理专用 SSL
当配置为使用自定义端口时，您还可以通过各种 `management.server.ssl.*` 属性为管理服务器配置其自身的 SSL。例如，这样做可以让管理服务器通过 HTTP 访问，而主应用程序则使用 HTTPS，如下面的属性设置所示：

```yaml

server:
  port: 8443
  ssl:
    enabled: true
    key-store: "classpath:store.jks"
    key-password: "secret"
management:
  server:
    port: 8080
    ssl:
      enabled: false
```

或者，主服务器和管理服务器都可以使用 SSL，但使用不同的密钥库，如下所示：

```yaml

server:
  port: 8443
  ssl:
    enabled: true
    key-store: "classpath:main.jks"
    key-password: "secret"
management:
  server:
    port: 8080
    ssl:
      enabled: true
      key-store: "classpath:management.jks"
      key-password: "secret"
```


# 4. 自定义管理服务器地址

您可以通过设置 `management.server.address` 属性来自定义管理端点可用的地址。如果您只想监听内部网络或运维网络，或者只想接受来自本地主机的连接，这样做会非常有用。 
只有在端口与主服务器端口不同时，您才能监听不同的地址。 
以下示例 `application.properties` 不允许远程管理连接：

```yaml

management:
  server:
    port: 8081
    address: "127.0.0.1"
```


# 5. 禁用 HTTP 端点
如果您不希望通过 HTTP 暴露端点，可以将管理端口设置为 -1，如下例所示：

```yaml

management:
  server:
    port: -1
```

您也可以通过使用 `management.endpoints.web.exposure.exclude` 属性来实现这一点，如下例所示：

```yaml
management:
  endpoints:
    web:
      exposure:
        exclude: "*"
```