package com.andreas.showsdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
