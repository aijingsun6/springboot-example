package cc.alking.example.springboot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SimpleCommandLineRunner implements CommandLineRunner, ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCommandLineRunner.class);

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("CommandLineRunner run with {}", (Object) args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("ApplicationRunner run with {}", (Object) args.getSourceArgs());
        LOGGER.info("ApplicationRunner run with {}", args.getNonOptionArgs());
    }
}
