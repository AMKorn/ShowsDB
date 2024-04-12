package com.andreas.showsdb.controller;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Getter
class NotFoundException extends Exception {
    private final ResponseEntity<Map<?, ?>> response;

    public NotFoundException(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        this.response = new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}