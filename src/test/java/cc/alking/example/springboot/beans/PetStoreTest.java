package cc.alking.example.springboot.beans;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PetStoreTest {

    @Test
    public void test(){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("petStore.xml");
        context.registerShutdownHook();
        PetStore petStore = context.getBean(PetStore.class);
        String name = petStore.getName();
        Assertions.assertEquals("PetStore", name);
    }

}
