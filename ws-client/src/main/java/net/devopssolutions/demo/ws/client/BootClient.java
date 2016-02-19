package net.devopssolutions.demo.ws.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public interface BootClient {

    static void main(String[] args) {
        SpringApplication.run(BootClient.class, args);
    }

}
