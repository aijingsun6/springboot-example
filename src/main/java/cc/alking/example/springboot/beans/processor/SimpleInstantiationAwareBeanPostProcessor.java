package cc.alking.example.springboot.beans.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class SimpleInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleInstantiationAwareBeanPostProcessor.class);


    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        LOGGER.debug("1. postProcessBeforeInstantiation,beanClass:{}, beanName:{}", beanClass, beanName);
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        LOGGER.debug("2. postProcessAfterInstantiation,bean:{},beanName:{}", bean, beanName);
        return true;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
            throws BeansException {
        LOGGER.debug("3. postProcessProperties,pvs:{},bean:{},beanName:{}", pvs, bean, beanName);
        return pvs;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        LOGGER.debug("4. postProcessBeforeInitialization,bean:{},beanName:{}", bean, beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        LOGGER.debug("5. postProcessAfterInitialization,bean:{},beanName:{}", bean, beanName);
        return bean;
    }

}
