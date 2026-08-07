# AbstractAutowireCapableBeanFactory 

```createBean``` 是创建 bean的入口函数，来自```AbstractBeanFactory.doGetBean```调用

几个关键步骤

- createBean:入口函数
- resolveBeforeInstantiation：它允许在 Spring 执行默认的实例化流程之前，通过特定的后置处理器（InstantiationAwareBeanPostProcessor）直接返回一个对象，从而完全跳过后续复杂的实例化、属性填充和初始化过程
- doCreateBean
- createBeanInstance：创建bean，此刻还没有属性，构造函数注入的属性除外
- determineCandidateConstructors:回调 SmartInstantiationAwareBeanPostProcessor获得构造函数
- applyMergedBeanDefinitionPostProcessors：回调 MergedBeanDefinitionPostProcessor接口
- addSingletonFactory/singletonFactories
- populateBean：填充属性


几个重要的回调接口：
InstantiationAwareBeanPostProcessor

```mermaid
---
title: createBean
---
flowchart TD
    A[Start] --> resolveBeforeInstantiation{resolveBeforeInstantiation返回非空?}
    resolveBeforeInstantiation --> |Yes| return 
    resolveBeforeInstantiation --> |No | createBeanInstance

    subgraph doCreateBean
    direction  TD
    createBeanInstance --> getInstanceSupplier{getInstanceSupplier非空?}
    getInstanceSupplier --> |Yes | obtainFromSupplier
    getInstanceSupplier --> |No | getFactoryMethodName{getFactoryMethodName非空?}
    getFactoryMethodName --> |Yes | instantiateUsingFactoryMethod
    getFactoryMethodName --> |No | resolvedConstructorOrFactoryMethod{resolvedConstructorOrFactoryMethod?}
    resolvedConstructorOrFactoryMethod --> |Yes | autowireNecessary{autowireNecessary为真?}
    autowireNecessary --> |Yes | autowireConstructor
    autowireNecessary --> |No | instantiateBean
    resolvedConstructorOrFactoryMethod --> determineConstructorsFromBeanPostProcessors{determineConstructorsFromBeanPostProcessors?}
    determineConstructorsFromBeanPostProcessors --> |Yes | autowireConstructor
    determineConstructorsFromBeanPostProcessors --> |No | getPreferredConstructors{getPreferredConstructors?}
    getPreferredConstructors --> | Yes | autowireConstructor
    getPreferredConstructors --> |No | instantiateBean
    end
```
```mermaid
---
title: populateBean
---
flowchart TD
    A[Start] --> postProcessAfterInstantiation{postProcessAfterInstantiation?}
    postProcessAfterInstantiation --> |No|return
    postProcessAfterInstantiation --> |Yes | postProcessProperties{postProcessProperties?}
    postProcessProperties -->|No | return
    postProcessProperties -->|Yes | pvs{pvs != null}
    pvs --> |Yes | applyPropertyValues
    pvs --> |No |return


```
