package cc.alking.example.springboot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.metrics.buffering.StartupTimeline;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SimpleCommandLineRunner implements CommandLineRunner, ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCommandLineRunner.class);

    private ApplicationContext context;

    @Autowired
    public SimpleCommandLineRunner(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("CommandLineRunner run with {}", (Object) args);
        profileStartUp();

    }

    private void profileStartUp(){
        if(context instanceof ConfigurableApplicationContext configurableApplicationContext){
            ApplicationStartup startup = configurableApplicationContext.getApplicationStartup();
            if (startup instanceof BufferingApplicationStartup bufferingApplicationStartup) {
                bufferingApplicationStartup.getBufferedTimeline().getEvents().forEach(item -> {
                    item.getStartupStep().getTags().forEach(tag -> LOGGER.info("tag,{}={}",tag.getKey(),tag.getValue()));
                    LOGGER.info("{} cost {} ms", item.getStartupStep().getName(), item.getDuration().toMillis());
                });
            }
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("ApplicationRunner run with {}", (Object) args.getSourceArgs());
        LOGGER.info("ApplicationRunner run with {}", args.getNonOptionArgs());
    }
}
