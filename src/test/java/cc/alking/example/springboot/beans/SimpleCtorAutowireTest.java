package cc.alking.example.springboot.beans;

import cc.alking.example.springboot.beans.simplectorauto.AA;
import cc.alking.example.springboot.beans.simplectorauto.BB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SimpleCtorAutowireTest {

    @Test
    public void test(){
        ApplicationContext context = new ClassPathXmlApplicationContext("simplectorauto.xml");
        AA a = context.getBean(AA.class);
        BB b = context.getBean(BB.class);
        Assertions.assertEquals(a,b.getAa());
    }
}
