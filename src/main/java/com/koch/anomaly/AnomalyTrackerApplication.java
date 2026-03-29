package com.koch.anomaly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.koch.anomaly", "com.koch.security"})
@EnableScheduling
public class AnomalyTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnomalyTrackerApplication.class, args);
    }
}
