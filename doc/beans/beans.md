# beans

bean是IoC重要的概念

核心围绕如下两个概念来展开
1. BeanFactory  提供了配置框架和基本功能
2. ApplicationContext 增加了更多面向企业的功能，是 BeanFactory 的完整超集

ApplicationContext负责实例化(instantiating)、配置(configuring)和组装(assembling ) Bean, 都是通过读取配置数据完成

# 0.目录 
## 0.1 配置数据(configuration metadata)的几种形式
### 0.1.1 Annotation-based配置
- [@Autowired](https://docs.spring.io/spring-framework/reference/6.2/core/beans/annotation-config/autowired.html)
- @PostConstruct
- @PreDestroy
- @Inject
- @Named

### 0.1.2 external XML files
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd">
    
    <import resource="services.xml"/>
	<import resource="resources/messageSource.xml"/>
	<import resource="/resources/themeSource.xml"/>

	<bean id="..." class="...">  
		<!-- collaborators and configuration for this bean go here -->
	</bean>

	<bean id="..." class="...">
		<!-- collaborators and configuration for this bean go here -->
	</bean>

	<!-- more bean definitions go here -->

</beans>



```


### 0.1.3 Groovy scripts
```
beans {
	dataSource(BasicDataSource) {
		driverClassName = "org.hsqldb.jdbcDriver"
		url = "jdbc:hsqldb:mem:grailsDB"
		username = "sa"
		password = ""
		settings = [mynew:"setting"]
	}
	sessionFactory(SessionFactory) {
		dataSource = dataSource
	}
	myService(MyService) {
		nestedBean = { AnotherBean bean ->
			dataSource = dataSource
		}
	}
}

```


### 0.1.4 Java-based configuration
- [@Configuration](https://docs.spring.io/spring-framework/docs/6.2.19/javadoc-api/org/springframework/context/annotation/Configuration.html)
- [@Bean](https://docs.spring.io/spring-framework/docs/6.2.19/javadoc-api/org/springframework/context/annotation/Bean.html)
- [@Import](https://docs.spring.io/spring-framework/docs/6.2.19/javadoc-api/org/springframework/context/annotation/Import.html)
- [@DependsOn](https://docs.spring.io/spring-framework/docs/6.2.19/javadoc-api/org/springframework/context/annotation/DependsOn.html) 




