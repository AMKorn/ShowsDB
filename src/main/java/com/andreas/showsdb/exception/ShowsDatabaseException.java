package com.andreas.showsdb.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ShowsDatabaseException extends Exception {
    private final ResponseEntity<Map<?,?>> response;

    public ShowsDatabaseException(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        this.response = new ResponseEntity<>(response, status);
    }

}
