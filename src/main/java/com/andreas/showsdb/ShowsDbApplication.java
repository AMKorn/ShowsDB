package com.andreas.showsdb;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class ShowsDbApplication {

//    @Autowired
//    RequestMappingHandlerMapping requestMappingHandlerMapping;

    public static void main(String[] args) {
        SpringApplication.run(ShowsDbApplication.class, args);
    }

//    @PostConstruct
//    public void printEndpoints() {
//        requestMappingHandlerMapping.getHandlerMethods().forEach((k,v) -> System.out.println(k + " : "+ v));
//    }
}
