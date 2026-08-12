package cc.alking.example.springboot.beans;

import cc.alking.example.springboot.beans.lifecycle.SimpleLifeCycle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SimpleLifeCycleTest {

    @Test
    public void test(){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("lifecycle.xml");
        context.registerShutdownHook();
        SimpleLifeCycle a = context.getBean(SimpleLifeCycle.class);
        Assertions.assertNotNull(a);
    }
}
