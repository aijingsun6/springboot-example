# Spring Beans and Dependency Injection

你可以使用任何标准的 Spring Framework 技术来定义你的 Bean 及其注入的依赖关系。我们通常建议使用构造器注入来配置依赖关系，并使用 @ComponentScan 来发现 Bean。

如果你按照上述建议组织代码（将应用程序类放在顶层包中），你可以添加不带任何参数的 @ComponentScan，或者使用隐式包含它的 @SpringBootApplication 注解。所有应用程序组件（@Component、@Service、@Repository、@Controller 等）都会自动注册为 Spring Bean。

以下示例展示了一个使用构造器注入来获取必需的 RiskAssessor Bean 的 @Service Bean：


```java


import org.springframework.stereotype.Service;

@Service
public class MyAccountService implements AccountService {

	private final RiskAssessor riskAssessor;

	public MyAccountService(RiskAssessor riskAssessor) {
		this.riskAssessor = riskAssessor;
	}

	// ...

}
```

如果一个bean有多个构造函数，你需要用@Autowired标记你希望Spring使用的那一个：

```java


import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyAccountService implements AccountService {

	private final RiskAssessor riskAssessor;

	private final PrintStream out;

	@Autowired
	public MyAccountService(RiskAssessor riskAssessor) {
		this.riskAssessor = riskAssessor;
		this.out = System.out;
	}

	public MyAccountService(RiskAssessor riskAssessor, PrintStream out) {
		this.riskAssessor = riskAssessor;
		this.out = out;
	}

	// ...

}
```

请注意，使用构造函数注入可以让 `riskAssessor` 字段被标记为 `final`，表示它之后不能被更改。