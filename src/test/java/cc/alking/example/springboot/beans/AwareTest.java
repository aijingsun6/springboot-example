package cc.alking.example.springboot.beans;


import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AwareTest {

    @Test
    public void test(){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("aware.xml");
        context.registerShutdownHook();
    }
}
