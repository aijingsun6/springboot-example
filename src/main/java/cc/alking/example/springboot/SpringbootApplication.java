package cc.alking.example.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

@SpringBootApplication
public class SpringbootApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootApplication.class);


	public static void main(String[] args) {
//		SpringApplication.run(SpringbootApplication.class, args);

        SpringApplication application = new SpringApplication(SpringbootApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
//        application.setWebApplicationType(WebApplicationType.NONE);

        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
	}

}
