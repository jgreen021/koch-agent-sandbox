package com.koch.anomaly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.koch.anomaly", "com.koch.security"})
public class AnomalyTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnomalyTrackerApplication.class, args);
    }
}
