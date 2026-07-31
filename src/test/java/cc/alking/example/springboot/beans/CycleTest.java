package cc.alking.example.springboot.beans;

import cc.alking.example.springboot.beans.cycle.CycleA;
import cc.alking.example.springboot.beans.cycle.CycleB;
import cc.alking.example.springboot.beans.cycle.CycleC;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CycleTest {

    @Test
    public void test(){
        ApplicationContext context = new ClassPathXmlApplicationContext("cycle.xml");
        CycleA a = context.getBean(CycleA.class);
        CycleB b = a.getCycleB();
        CycleC c = b.getCycleC();
        Assertions.assertEquals(a, c.getCycleA());
    }
}
