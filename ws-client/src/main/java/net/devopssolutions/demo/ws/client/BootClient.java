package net.devopssolutions.demo.ws.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BootClient {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BootClient.class, args);

        Thread.currentThread().join();
    }

}
