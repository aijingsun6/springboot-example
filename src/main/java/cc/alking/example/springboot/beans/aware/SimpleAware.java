package cc.alking.example.springboot.beans.aware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SimpleAware implements ApplicationContextAware, BeanNameAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAware.class);

    @Override
    public void setBeanName(String name) {
        LOGGER.info("setBeanName {}", name);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LOGGER.info("setApplicationContext {}", applicationContext);
    }
}
