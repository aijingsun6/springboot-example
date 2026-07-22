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
```mermaid

classDiagram
    AbstractApplicationContext <|--AbstractRefreshableApplicationContext:extends
    AbstractRefreshableApplicationContext <|-- AbstractRefreshableConfigApplicationContext:extends
    AbstractRefreshableConfigApplicationContext <|-- AbstractXmlApplicationContext:extends
    AbstractXmlApplicationContext <|-- ClassPathXmlApplicationContext:extends

class AbstractApplicationContext {
  + void refresh()
    abstract void refreshBeanFactory() 
}

class AbstractRefreshableApplicationContext {
    void refreshBeanFactory()
}
```

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

