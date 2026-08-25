package cc.alking.example.springboot.aop;


import cc.alking.example.springboot.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AccountAspectTest {

    @Test
    public void test(){

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("account_aspect.xml");
        context.registerShutdownHook();
        AccountService as = context.getBean(AccountService.class);
        as.register("name","password");

    }
}
