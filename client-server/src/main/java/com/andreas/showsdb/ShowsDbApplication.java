package com.andreas.showsdb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ShowsDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShowsDbApplication.class, args);
    }
}
