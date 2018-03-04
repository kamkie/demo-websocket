package net.devopssolutions.demo.ws.client;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BootClient {

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(BootClient.class).web(WebApplicationType.NONE).run(args);

        Thread.currentThread().join();
    }

}
