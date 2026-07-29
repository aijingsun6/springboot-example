# BeanDefinitionDocumentReader

用于解析包含Spring bean定义的XML文档 由XmlBeanDefinitionReader用于实际解析DOM文档

example:

```java
// XmlBeanDefinitionReader.java
	public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
		BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
		int countBefore = getRegistry().getBeanDefinitionCount();
		documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
		return getRegistry().getBeanDefinitionCount() - countBefore;
	}
```
```mermaid
classDiagram
    BeanDefinitionDocumentReader <|--DefaultBeanDefinitionDocumentReader:implements

class BeanDefinitionDocumentReader {
    <<interface>>
    void registerBeanDefinitions(Document doc, XmlReaderContext readerContext)
}
class DefaultBeanDefinitionDocumentReader {
    XmlReaderContext readerContext
    BeanDefinitionParserDelegate delegate

    void preProcessXml(Element root)//子类自定义预埋点

    void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) // 真正读取bean定义

    void postProcessXml(Element root)//子类自定义预埋点

    void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate)//挑选node节点类型

    void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate)// parse BeanDefinitionHolder
    
}
```

实际parse BeanDefinitionHolder依赖BeanDefinitionParserDelegate.java
```mermaid
classDiagram
   class BeanDefinitionParserDelegate {
      + BeanDefinitionHolder parseBeanDefinitionElement(Element ele)
   }

```


```mermaid
classDiagram
ReaderContext <|-- XmlReaderContext:extends

class ReaderContext {
    Resource resource
    ProblemReporter problemReporter
    ReaderEventListener eventListener
     SourceExtractor sourceExtractor
}

class XmlReaderContext {
    XmlBeanDefinitionReader reader
    NamespaceHandlerResolver namespaceHandlerResolver
}

```