# ClassPathXmlApplicationContext

## 1.继承关系图
```mermaid
classDiagram
    ApplicationContext <|-- ConfigurableApplicationContext:extends
    Lifecycle <|-- ConfigurableApplicationContext:extends
    ResourceLoader <|-- DefaultResourceLoader:implements
    DefaultResourceLoader <|-- AbstractApplicationContext:extends
    ConfigurableApplicationContext <|-- AbstractApplicationContext:implements
    AbstractApplicationContext <|--AbstractRefreshableApplicationContext:extends
    AbstractRefreshableApplicationContext <|-- AbstractRefreshableConfigApplicationContext:extends
    AbstractRefreshableConfigApplicationContext <|-- AbstractXmlApplicationContext:extends
    AbstractXmlApplicationContext <|-- ClassPathXmlApplicationContext:extends
```

## 2. refresh

## 2.1 是如何加载bean定义的
```
// AbstractXmlApplicationContext.java

	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		beanDefinitionReader.setEnvironment(getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		initBeanDefinitionReader(beanDefinitionReader);
		loadBeanDefinitions(beanDefinitionReader);
	}
```
执行完成后 beanDefinitionMap 是有值的

调用栈如下
```
AbstractApplicationContext.java
refresh()
obtainFreshBeanFactory()
refreshBeanFactory()

AbstractRefreshableApplicationContext.java
refreshBeanFactory()
loadBeanDefinitions()

AbstractXmlApplicationContext.java
loadBeanDefinitions()

```

## 2.2 bean 是如何初始化的
```

```
调用栈如下
```
AbstractApplicationContext.java
refresh()
finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory)
beanFactory.preInstantiateSingletons();


DefaultListableBeanFactory.java
preInstantiateSingletons()
instantiateSingleton()

AbstractBeanFactory.java
getBean()


```