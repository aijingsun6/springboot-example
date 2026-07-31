# 特殊的bean

# ClassPathXmlApplicationContext

| 名称 | 类型 |阶段 | 可选|
| --- | --- | --- | --- |
| environment | StandardEnvironmen  |prepareBeanFactory| N|
| systemProperties | Properties  |prepareBeanFactory|N|
| systemEnvironment | Map  |prepareBeanFactory |N|
| applicationStartup |DefaultApplicationStartup  |prepareBeanFactory|N|
| messageSource |DelegatingMessageSource|initMessageSource|N|
| applicationEventMulticaster |SimpleApplicationEventMulticaster | initApplicationEventMulticaster|N|
|bootstrapExecutor |Executor |finishBeanFactoryInitialization | Y |
| conversionService |ConversionService |finishBeanFactoryInitialization | Y |