# DefaultListableBeanFactory

# 1.继承关系
```mermaid
classDiagram
    SimpleAliasRegistry <|--DefaultSingletonBeanRegistry:extends
    DefaultSingletonBeanRegistry <|--FactoryBeanRegistrySupport:extends
    FactoryBeanRegistrySupport <|-- AbstractBeanFactory:extends
    AbstractBeanFactory <|-- AbstractAutowireCapableBeanFactory:extends
    AbstractAutowireCapableBeanFactory <|-- DefaultListableBeanFactory:extends

    AliasRegistry <|-- SimpleAliasRegistry:implements
    SingletonBeanRegistry <|-- DefaultSingletonBeanRegistry:implements
    ConfigurableBeanFactory <|--AbstractBeanFactory:implements
    AutowireCapableBeanFactory <|-- AbstractAutowireCapableBeanFactory:implements
    ConfigurableListableBeanFactory <|-- DefaultListableBeanFactory:implements
    BeanDefinitionRegistry <|-- DefaultListableBeanFactory:implements

```

## 1.1 AliasRegistry(interface)
简单的来说就是一个别名(alias)的管理

```mermaid
classDiagram
class AliasRegistry {
    void registerAlias(String name, String alias)
    void removeAlias(String alias)
    boolean isAlias(String name)
    String[] getAliases(String name)
}

```
## 1.2 SingletonBeanRegistry(interface)
定义共享bean实例注册表的接口。org.springframework.beans.factory.BeanFactory的实现类可以实现此接口，以便以统一方式暴露其单例管理功能。
就是一个单例的增删改查器

```mermaid
classDiagram
class SingletonBeanRegistry {
    void registerSingleton(String beanName, Object singletonObject)
    void addSingletonCallback(String beanName, Consumer singletonConsumer)
    Object getSingleton(String beanName)
    boolean containsSingleton(String beanName)
    String[] getSingletonNames()
    int getSingletonCount()
}

```
## 1.3 ConfigurableBeanFactory(interface)
```mermaid
classDiagram
HierarchicalBeanFactory <|-- ConfigurableBeanFactory:extends
SingletonBeanRegistry <|-- ConfigurableBeanFactory:extends
BeanFactory <|-- HierarchicalBeanFactory:extends
class ConfigurableBeanFactory {
    void setParentBeanFactory(BeanFactory parentBeanFactory) 

}
```

## 1.4 BeanDefinitionRegistry
```mermaid
classDiagram
class BeanDefinitionRegistry {
    <<interface>>
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
	void removeBeanDefinition(String beanName)
    BeanDefinition getBeanDefinition(String beanName)
    boolean containsBeanDefinition(String beanName)
    String[] getBeanDefinitionNames()
    int getBeanDefinitionCount()
    boolean isBeanDefinitionOverridable(String beanName)
    boolean isBeanNameInUse(String beanName)
}

```