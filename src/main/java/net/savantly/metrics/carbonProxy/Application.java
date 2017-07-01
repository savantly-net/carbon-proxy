package net.savantly.metrics.carbonProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.hawt.springboot.EnableHawtio;
import io.hawt.web.AuthenticationFilter;

@EnableHawtio
@SpringBootApplication
public class Application implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(Application.class);
    
    public static void main(String[] args) {
    	if(!System.getProperties().containsKey(AuthenticationFilter.HAWTIO_AUTHENTICATION_ENABLED)){
    		System.setProperty(AuthenticationFilter.HAWTIO_AUTHENTICATION_ENABLED, "false");
    	}
        SpringApplication.run(Application.class, args);
    }

    public void run(String... args) throws Exception {
    }

}