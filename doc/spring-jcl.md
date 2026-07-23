# spring-jcl
# 0.spring-jcl是什么？
简单来说 spring-jcl 是日志门面（Logging Facade）模块

其中继承了主流的日志框架
- slf4j
- log4j
- java.util.logging

# 1. 实例
```
public class MyBean {
	private final Log log = LogFactory.getLog(getClass());
	// ...
}
```

# 2.是如何实现日志门面的?
主要核心逻辑如下
```
// 使用日志的时候添加Logger
protected final Log logger = LogFactory.getLog(getClass());

// 实际调用的是 LogAdapter.java
	public static Log createLog(String name) {
		return createLog.apply(name);
	}
// 如何实现门面的,看 createLog 的实现方法

private static final boolean log4jSpiPresent = isPresent("org.apache.logging.log4j.spi.ExtendedLogger");

	private static final boolean log4jSlf4jProviderPresent = isPresent("org.apache.logging.slf4j.SLF4JProvider");

	private static final boolean slf4jSpiPresent = isPresent("org.slf4j.spi.LocationAwareLogger");

	private static final boolean slf4jApiPresent = isPresent("org.slf4j.Logger");


	private static final Function<String, Log> createLog;

	static {
		if (log4jSpiPresent) {
			if (log4jSlf4jProviderPresent && slf4jSpiPresent) {
				// log4j-to-slf4j bridge -> we'll rather go with the SLF4J SPI;
				// however, we still prefer Log4j over the plain SLF4J API since
				// the latter does not have location awareness support.
				createLog = Slf4jAdapter::createLocationAwareLog;
			}
			else {
				// Use Log4j 2.x directly, including location awareness support
				createLog = Log4jAdapter::createLog;
			}
		}
		else if (slf4jSpiPresent) {
			// Full SLF4J SPI including location awareness support
			createLog = Slf4jAdapter::createLocationAwareLog;
		}
		else if (slf4jApiPresent) {
			// Minimal SLF4J API without location awareness support
			createLog = Slf4jAdapter::createLog;
		}
		else {
			// java.util.logging as default
			// Defensively use lazy-initializing adapter class here as well since the
			// java.logging module is not present by default on JDK 9. We are requiring
			// its presence if neither Log4j nor SLF4J is available; however, in the
			// case of Log4j or SLF4J, we are trying to prevent early initialization
			// of the JavaUtilLog adapter - for example, by a JVM in debug mode - when eagerly
			// trying to parse the bytecode for all the cases of this switch clause.
			createLog = JavaUtilAdapter::createLog;
		}
	}

// 从代码能清晰的看出日志门面的逻辑
```
# 3. 参考文档
- [spring Logging](https://docs.spring.io/spring-framework/reference/6.2/core/spring-jcl.html)
- [spring boot Logging](https://docs.spring.io/spring-boot/reference/features/logging.html)