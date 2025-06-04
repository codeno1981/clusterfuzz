package com.google.clusterfuzz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for ClusterFuzz Java.
 */
@SpringBootApplication
@EnableScheduling
public class ClusterFuzzApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClusterFuzzApplication.class, args);
    }
}