# HTTP Message Conversion 

spring-web 模块包含了 `HttpMessageConverter` 接口，用于通过 `InputStream` 和 `OutputStream` 读写 HTTP 请求和响应的主体。`HttpMessageConverter` 实例在客户端（例如在 RestClient 中）和服务器端（例如在 Spring MVC 的 REST 控制器中）都有使用。

框架中提供了针对主要媒体类型（MIME 类型）的具体实现，并且默认情况下，这些实现会注册到客户端的 RestClient 和 RestTemplate 中，以及服务器端的 RequestMappingHandlerAdapter 中（参见配置消息转换器）。

下面描述了几种 `HttpMessageConverter` 的实现。完整的列表请参考 `HttpMessageConverter` 的 Javadoc。对于所有转换器，都会使用默认的媒体类型，但您可以通过设置 `supportedMediaTypes` 属性来覆盖它。

|MessageConverter|	Description|
| --- | --- |
|StringHttpMessageConverter|一个HttpMessageConverter实现，可以从HTTP请求和响应中读取和写入String实例。默认情况下，该转换器支持所有文本媒体类型（text/*），并使用text/plain的Content-Type进行写入。|
|FormHttpMessageConverter|一个 HttpMessageConverter 的实现，能够从 HTTP 请求和响应中读取和写入表单数据。默认情况下，该转换器读取和写入 application/x-www-form-urlencoded 媒体类型。表单数据从 MultiValueMap<String, String> 中读取并写入其中。该转换器还可以写入（但不能读取）从 MultiValueMap<String, Object> 中读取的 multipart 数据。默认情况下，支持 multipart/form-data。可以支持其他 multipart 子类型来写入表单数据。有关更多详细信息，请参阅 FormHttpMessageConverter 的 JavaDoc。|
|ByteArrayHttpMessageConverter|一个 HttpMessageConverter 的实现，可以从 HTTP 请求和响应中读取和写入字节数组。默认情况下，此转换器支持所有媒体类型（*/*），并以 Content-Type 为 application/octet-stream 的方式写入。您可以通过设置 supportedMediaTypes 属性并重写 getContentType(byte[]) 方法来覆盖此行为。|
|MarshallingHttpMessageConverter|一个 HttpMessageConverter 的实现，它能够通过使用 Spring 的 Marshaller 和 Unmarshaller 抽象（来自 org.springframework.oxm 包）来读写 XML。在使用该转换器之前，必须先配置一个 Marshaller 和 Unmarshaller。你可以通过构造函数或 bean 属性来注入这些组件。默认情况下，该转换器支持 text/xml 和 application/xml 格式。|
|MappingJackson2HttpMessageConverter|一种使用 Jackson 的 ObjectMapper 来读写 JSON 的 HttpMessageConverter 实现。您可以通过使用 Jackson 提供的注解来自定义 JSON 映射。当您需要进一步控制（例如需要为特定类型提供自定义 JSON 序列化器/反序列化器的情况）时，可以通过 ObjectMapper 属性注入自定义的 ObjectMapper。默认情况下，该转换器支持 application/json。这需要 com.fasterxml.jackson.core:jackson-databind 依赖。|
|MappingJackson2XmlHttpMessageConverter|一种 HttpMessageConverter 实现，可通过使用 Jackson XML 扩展的 XmlMapper 来读写 XML。您可以通过使用 JAXB 或 Jackson 提供的注解来自定义 XML 映射。当您需要进一步控制（例如需要为特定类型提供自定义 XML 序列化器/反序列化器的情况）时，可以通过 ObjectMapper 属性注入自定义的 XmlMapper。默认情况下，此转换器支持 application/xml。这需要 com.fasterxml.jackson.dataformat:jackson-dataformat-xml 依赖。|
| MappingJackson2CborHttpMessageConverter|com.fasterxml.jackson.dataformat:jackson-dataformat-cbor|
|SourceHttpMessageConverter |一个 HttpMessageConverter 的实现，可以从 HTTP 请求和响应中读取和写入 javax.xml.transform.Source。仅支持 DOMSource、SAXSource 和 StreamSource。默认情况下，此转换器支持 text/xml 和 application/xml。|
|GsonHttpMessageConverter|一种 HttpMessageConverter 实现，可通过使用 "Google Gson" 来读取和写入 JSON。这需要 com.google.code.gson:gson 依赖项。|
|JsonbHttpMessageConverter|一个 HttpMessageConverter 实现，能够使用 Jakarta Json Bind API 读取和写入 JSON。这需要依赖 jakarta.json.bind:jakarta.json.bind-api 以及可用的实现。|
| ProtobufHttpMessageConverter|一个 HttpMessageConverter 实现，能够以二进制格式读取和写入具有 "application/x-protobuf" 内容类型的 Protobuf 消息。这需要依赖 com.google.protobuf:protobuf-java。|
|ProtobufJsonFormatHttpMessageConverter|一个 HttpMessageConverter 的实现，能够将 JSON 文档与 Protobuf 消息之间进行读写。这需要依赖 com.google.protobuf:protobuf-java-util。|



# 参考文档
- [HttpMessageConverter](https://docs.spring.io/spring-framework/docs/6.2.19/javadoc-api/org/springframework/http/converter/HttpMessageConverter.html)