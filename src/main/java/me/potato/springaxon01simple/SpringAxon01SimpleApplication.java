package me.potato.springaxon01simple;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringAxon01SimpleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAxon01SimpleApplication.class, args);
    }

    @EventHandler
    public void on(Object event) {
        log.info("Event: {}", event);
    }
}
