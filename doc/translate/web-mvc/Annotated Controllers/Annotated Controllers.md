# Annotated Controllers(注解)

Spring MVC 提供了一种基于注解的编程模型，其中 @Controller 和 @RestController 组件使用注解来表达请求映射、请求输入、异常处理等功能。注解式控制器具有灵活的方法签名，无需继承基类，也无需实现特定接口。以下示例展示了一个通过注解定义的控制器：

```java

@Controller
public class HelloController {

	@GetMapping("/hello")
	public String handle(Model model) {
		model.addAttribute("message", "Hello World!");
		return "index";
	}
}
```
在前面的示例中，该方法接受一个 Model 并返回一个字符串形式的视图名称，但还存在许多其他选项，这些选项将在本章后面进行解释。

spring.io 上的指南和教程使用了本节中描述的基于注解的编程模型。




# 附录
| 注解| 说明 |
| --- | --- |
| @RestController |Controller + ResponseBody|
| @Controller | |
| @ResponseBody | 直接将数据写入响应体，而不是通过 HTML 模板进行视图解析和渲染 |
| @PathVariable | 捕获的 URI 变量可以通过 @PathVariable 进行访问 |
| @GetMapping
| @PostMapping
| @PutMapping
| @DeleteMapping
| @PatchMapping

# 参考文档


- [URI patterns](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-requestmapping.html#mvc-ann-requestmapping-uri-templates)