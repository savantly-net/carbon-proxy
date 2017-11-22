package net.savantly.metrics.carbonProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(Application.class);
    
    public static void main(String[] args) {
    	log.info("Starting application main()");
        SpringApplication.run(Application.class, args);
    }

    public void run(String... args) throws Exception {
    }

}