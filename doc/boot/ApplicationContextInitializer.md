# ApplicationContextInitializer
| name | location |initialize |
| --- | --- | --- |
| SharedMetadataReaderFactoryContextInitializer | org.springframework.boot.autoconfigure|添加 BeanFactoryPostProcessor |
| ContextIdApplicationContextInitializer | org.springframework.boot.context| 添加单例 ContextId ，注：这个早于refreshContext |
| ConditionEvaluationReportLoggingListener |org.springframework.boot.autoconfigure.logging |applicationContext.addApplicationListener(new ConditionEvaluationReportListener(applicationContext));|
| ConfigurationWarningsApplicationContextInitializer |org.springframework.boot.context |ontext.addBeanFactoryPostProcessor(new ConfigurationWarningsPostProcessor(getChecks())); |
| ProtocolResolverApplicationContextInitializer | org.springframework.boot.io |context.addProtocolResolver |
| RSocketPortInfoApplicationContextInitializer |org.springframework.boot.rsocket.context| applicationContext.addApplicationListener(new Listener(applicationContext))|
| ServerPortInfoApplicationContextInitializer | org.springframework.boot.web.context| applicationContext.addApplicationListener |


# 1. SharedMetadataReaderFactoryContextInitializer

```bash
public void initialize(ConfigurableApplicationContext applicationContext) {
		if (AotDetector.useGeneratedArtifacts()) {
			return;
		}
		BeanFactoryPostProcessor postProcessor = new CachingMetadataReaderFactoryPostProcessor(applicationContext);
		applicationContext.addBeanFactoryPostProcessor(postProcessor);
	}
```

# 2. ContextIdApplicationContextInitializer

# 3. ConditionEvaluationReportLoggingListener

主要提供 ContextRefreshedEvent， ApplicationFailedEvent 这两种事件的report

```java

        @Override
		public void onApplicationEvent(ApplicationEvent event) {
			if (event instanceof ContextRefreshedEvent contextRefreshedEvent) {
				if (contextRefreshedEvent.getApplicationContext() == this.context) {
					this.logger.logReport(false);
				}
			}
			else if (event instanceof ApplicationFailedEvent applicationFailedEvent
					&& applicationFailedEvent.getApplicationContext() == this.context) {
				this.logger.logReport(true);
			}
		}
```

# 4. ConfigurationWarningsApplicationContextInitializer

# 5. ProtocolResolverApplicationContextInitializer

# 6. RSocketPortInfoApplicationContextInitializer

```java
    @Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		applicationContext.addApplicationListener(new Listener(applicationContext));
	}
```

# 7. ServerPortInfoApplicationContextInitializer


