package net.devopssolutions.demo.ws.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public interface BootServer {

    static void main(String[] args) {
        SpringApplication.run(BootServer.class, args);
    }

}
