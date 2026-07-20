package cc.alking.example.springboot.beans;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PetStoreTest {

    @Test
    public void test(){
        ApplicationContext context = new ClassPathXmlApplicationContext("petStore.xml");
        PetStore petStore = context.getBean(PetStore.class);
        String name = petStore.getName();
        Assertions.assertEquals("PetStore", name);
    }

}
