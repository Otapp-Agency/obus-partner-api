package com.obuspartners;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for OBUS Partners API
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.obuspartners"})
@EnableScheduling
public class ObusPartnersApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObusPartnersApiApplication.class, args);
    }
}