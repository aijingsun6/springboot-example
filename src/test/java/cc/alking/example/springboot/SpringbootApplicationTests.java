package cc.alking.example.springboot;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(classes = SpringbootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SpringbootApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootApplicationTests.class);

    @Autowired
    private ApplicationContext ctx;

	@Test
	void contextLoads() {
	    LOGGER.info("ApplicationContext {}", ctx.getClass());
    }

}
