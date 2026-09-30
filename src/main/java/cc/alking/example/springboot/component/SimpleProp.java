package cc.alking.example.springboot.component;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimpleProp {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProp.class);

    @Value("${foo}")
    private String foo;

    @PostConstruct
    public void init(){
        LOGGER.info("foo = {}", foo);
    }
}
