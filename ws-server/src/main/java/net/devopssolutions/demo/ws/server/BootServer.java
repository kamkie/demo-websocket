package net.devopssolutions.demo.ws.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BootServer {

    public static void main(String[] args) {
        SpringApplication.run(BootServer.class, args);
    }

}
