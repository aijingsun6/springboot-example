# DefaultSingletonBeanRegistry

# 1. 继承关系

```mermaid
classDiagram

    SimpleAliasRegistry <|-- DefaultSingletonBeanRegistry:extends
    SingletonBeanRegistry <|-- DefaultSingletonBeanRegistry:implements
    AliasRegistry <|-- SimpleAliasRegistry:implements
    DefaultSingletonBeanRegistry <|-- FactoryBeanRegistrySupport:extends
    FactoryBeanRegistrySupport <|-- AbstractBeanFactory:extends

    class SingletonBeanRegistry {
        <<interface>>
    }
    class AliasRegistry {
        <<interface>>
    }

```
DefaultSingletonBeanRegistry 本质上是bean仓库,不负责创建销毁的过程

# 2.增删改查
全程围绕着几    个属性
```
    /** Cache of singleton objects: bean name to bean instance. */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	/** Creation-time registry of singleton factories: bean name to ObjectFactory. */
	private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(16);

	/** Cache of early singleton objects: bean name to bean instance. */
	private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

	/** Set of registered singletons, containing the bean names in registration order. */
	private final Set<String> registeredSingletons = Collections.synchronizedSet(new LinkedHashSet<>(256));
```

## 2.1 bean 添加
```java
    // 线程安全的 addSingleton
    @Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		Assert.notNull(beanName, "Bean name must not be null");
		Assert.notNull(singletonObject, "Singleton object must not be null");
		this.singletonLock.lock();
		try {
			addSingleton(beanName, singletonObject);
		}
		finally {
			this.singletonLock.unlock();
		}
	}

protected void addSingleton(String beanName, Object singletonObject) {
		Object oldObject = this.singletonObjects.putIfAbsent(beanName, singletonObject);
		if (oldObject != null) {
			throw new IllegalStateException("Could not register object [" + singletonObject +
					"] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
		}
		this.singletonFactories.remove(beanName);
		this.earlySingletonObjects.remove(beanName);
		this.registeredSingletons.add(beanName);

		Consumer<Object> callback = this.singletonCallbacks.get(beanName);
		if (callback != null) {
			callback.accept(singletonObject);
		}
	}
```
## 2.2 bean 删除
```java
protected void removeSingleton(String beanName) {
		this.singletonObjects.remove(beanName);
		this.singletonFactories.remove(beanName);
		this.earlySingletonObjects.remove(beanName);
		this.registeredSingletons.remove(beanName);
	}

```
## 2.3 核心方法 getSingleton(String beanName, ObjectFactory<?> singletonFactory)
```java
public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory)

// 添加到 Set<String> singletonsCurrentlyInCreation 
protected void beforeSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
			throw new BeanCurrentlyInCreationException(beanName);
		}
	}

术语：
一级缓存：singletonObjects
二级缓存：earlySingletonObjects
三级缓存：singletonFactories
宽松模式:
1.isCurrentThreadAllowedToHoldSingletonLock 子类返回 true
2.this.singletonLock.tryLock() 失败

```

```mermaid
flowchart TD
    A[开始] --> B{一级缓存有没有?}
    B -- Yes --> C[返回]
    B -- No --> D{宽松模式?}
    D --Yes -->E{当前线程获取锁成功}
    D --No --> H{是否在销毁中?}
    E --Yes -->F[宽松模式添加beanName]
    F --> H
    E -- No -->G{缓存有没有?}
    G -- Yes --> End[返回]
    G --- No --> H
    H -- Yes --> I[返回]
    H -- No --> J[beforeSingletonCreation]
    J --> K{一级缓存有没有?}
    K -- Yes --> L[afterSingletonCreation]
    K -- No --> KK[singletonFactory初始化bean]
    KK --> L
    L --> M{是否新创bean?}
    M --> Yes --> N[回放到一级缓存]
    N --> O[宽松模式删除beanName]
    O --> P[结束]
```
总结：
1. 该方法的操作对象是一级缓存

## 2.4 核心方法 getSingleton(String beanName, boolean allowEarlyReference) 
术语：
- 一级缓存：singletonObjects
- 二级缓存：earlySingletonObjects
- 三级缓存：singletonFactories

```mermaid
flowchart TD
Start[开始] --> A{一级缓存有没有?}
A -- Yes --> B[return]
A -- No --> C{是否在创建中?}
C -- Yes --> B
C -- No --> E{二级缓存有没有?}
E -- Yes -->B
E -- Noe -->G{加锁成功?}
G -- Yes -->I{一级缓存有没有?}
G -- No --> B
I --Yes -->B
I --No --> K{二级缓存有没有?}
K -- Yes --> B
K -- No -->M[三级缓存有没有?]
M --Yes -->N[创建bean]
M --No -->B
N --> O[放二级缓存，删三级缓存]
O --> B
```
总结：
1. 这个方法与1，2，3级缓存均有交互