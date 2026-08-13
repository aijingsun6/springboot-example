# 特殊的bean

# 1.ClassPathXmlApplicationContext

| 名称 | 类型 |阶段 | 可选|
| --- | --- | --- | --- |
| environment | StandardEnvironmen  |prepareBeanFactory| N|
| systemProperties | Properties  |prepareBeanFactory|N|
| systemEnvironment | Map  |prepareBeanFactory |N|
| applicationStartup |DefaultApplicationStartup  |prepareBeanFactory|N|
| messageSource |DelegatingMessageSource|initMessageSource|N|
| applicationEventMulticaster |SimpleApplicationEventMulticaster | initApplicationEventMulticaster|N|
| lifecycleProcessor |DefaultLifecycleProcessor |finishRefresh |N |


# 2.AnnotationConfigServletWebServerApplicationContext
spring boot默认使用的context


| 名称 | 类型 |阶段 | 
| --- | --- | --- |
| org.springframework.boot.context.ContextIdApplicationContextInitializer$ContextId | ContextId  |prepareContext|
| autoConfigurationReport |ConditionEvaluationReport | prepareContext| 
| springApplicationArguments |DefaultApplicationArguments | prepareContext|
| springBootBanner |SpringApplicationBannerPrinter$PrintedBanner |prepareContext |
| springBootLoggingSystem |LoggingSystem/LogbackLoggingSystem |prepareContext | 
| springBootLoggerGroups|LoggerGroups  |prepareContext |
| springBootLoggingLifecycle | LoggingApplicationListener$Lifecycle |prepareContext |
| environment |ApplicationServletEnvironment |prepareBeanFactory|
| systemProperties |Properties |prepareBeanFactory |
| systemEnvironment |UnmodifiableMap |prepareBeanFactory | 
| applicationStartup |DefaultApplicationStartup |prepareBeanFactory | 
| org.springframework.context.annotation.ConfigurationClassPostProcessor.importRegistry |ConfigurationClassParser$ImportStack |

## 2.1 BeanDefinitionRegistryPostProcessor

| 名称 | 类型 | 
| --- | --- | 
| org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory | SharedMetadataReaderFactoryContextInitializer$SharedMetadataReaderFactoryBean |
| org.springframework.context.annotation.internalConfigurationAnnotationProcessor |ConfigurationClassPostProcessor|

## 2.2 BeanFactoryPostProcessor
| 名称 | 类型 | 
| --- | --- | 
| propertySourcesPlaceholderConfigurer |PropertySourcesPlaceholderConfigurer |
| org.springframework.boot.sql.init.dependency.DatabaseInitializationDependencyConfigurer$DependsOnDatabaseInitializationPostProcessor | DatabaseInitializationDependencyConfigurer$DependsOnDatabaseInitializationPostProcessor|