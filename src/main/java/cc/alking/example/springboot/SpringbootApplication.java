package cc.alking.example.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

@SpringBootApplication
public class SpringbootApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootApplication.class);


	public static void main(String[] args) {
//		SpringApplication.run(SpringbootApplication.class, args);

        SpringApplication application = new SpringApplication(SpringbootApplication.class);
        application.addListeners(new ApplicationListener<ApplicationEvent>() {
            @Override
            public void onApplicationEvent(ApplicationEvent event) {
                LOGGER.info("ApplicationEvent:{}", event);
            }
        });
        application.setBannerMode(Banner.Mode.OFF);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
	}

}
