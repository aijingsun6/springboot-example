package cc.alking.example.springboot.beans.lifecycle;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class SimpleLifeCycle implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleLifeCycle.class);


    @PostConstruct
    public void init(){
        LOGGER.info("PostConstruct");
    }

    @PreDestroy
    public void close(){
        LOGGER.info("PreDestroy");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("afterPropertiesSet");
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.info("destroy");
    }
}
