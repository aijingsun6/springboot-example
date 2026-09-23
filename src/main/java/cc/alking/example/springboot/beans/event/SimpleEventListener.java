package cc.alking.example.springboot.beans.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class SimpleEventListener  implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEventListener.class);

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        LOGGER.info("onApplicationEvent {}", event);
    }
}
