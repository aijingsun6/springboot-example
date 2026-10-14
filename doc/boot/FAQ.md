# FAQ

# 1. WebApplicationType
## 1.1 如何判断 WebApplicationType
```
private static final String[] SERVLET_INDICATOR_CLASSES = { "jakarta.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext" };

	private static final String WEBMVC_INDICATOR_CLASS = "org.springframework.web.servlet.DispatcherServlet";

	private static final String WEBFLUX_INDICATOR_CLASS = "org.springframework.web.reactive.DispatcherHandler";

	private static final String JERSEY_INDICATOR_CLASS = "org.glassfish.jersey.servlet.ServletContainer";

	static WebApplicationType deduceFromClasspath() {
		if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
				&& !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
			return WebApplicationType.REACTIVE;
		}
		for (String className : SERVLET_INDICATOR_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) {
				return WebApplicationType.NONE;
			}
		}
		return WebApplicationType.SERVLET;
	}

```
## 1.2 如何设置 WebApplicationType
### 1.2.1 使用java代码设置 WebApplicationType
```java
	public static void main(String[] args) {
//		SpringApplication.run(SpringbootApplication.class, args);

        SpringApplication application = new SpringApplication(SpringbootApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.setWebApplicationType(WebApplicationType.NONE);//代码设置类型

        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
	}
```
### 1.2.2 使用配置文件设置 WebApplicationType
```yaml

spring:
  main:
    web-application-type: "none"
    banner-mode: "off"
```
