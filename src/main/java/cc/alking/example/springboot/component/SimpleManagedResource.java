package cc.alking.example.springboot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource
public class SimpleManagedResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleManagedResource.class);

    private int counter = 0;

    @ManagedAttribute(description = "计数器")
    public int getCounter() {
        LOGGER.info("getCounter {}", counter);
        return counter;
    }

    @ManagedAttribute
    public void setCounter(int counter) {
        LOGGER.info("setCounter {}", counter);
        this.counter = counter;
    }

    @ManagedOperation(description = "自增")
    public int increment() {
        LOGGER.info("increment {}", counter);
        return ++counter;
    }
}
