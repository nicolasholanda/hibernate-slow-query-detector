package com.estudos.slowquerydetector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SlowQueryDetectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlowQueryDetectorApplication.class, args);
    }
}
