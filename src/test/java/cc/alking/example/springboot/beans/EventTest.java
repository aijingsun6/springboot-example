package cc.alking.example.springboot.beans;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class EventTest {

    @Test
    public void test(){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("event.xml");
        context.registerShutdownHook();
    }
}
