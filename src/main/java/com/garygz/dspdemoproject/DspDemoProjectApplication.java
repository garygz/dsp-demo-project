package com.garygz.dspdemoproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DspDemoProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(DspDemoProjectApplication.class, args);
    }

}
