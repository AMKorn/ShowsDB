package com.andreas.showsdb.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class ShowsDatabaseException extends Exception {
    private final ResponseEntity<ExceptionMessage> response;

    public ShowsDatabaseException(HttpStatus status) {
        this.response = new ResponseEntity<>(status);
    }

    public ShowsDatabaseException(String message, HttpStatus status) {
        this.response = new ResponseEntity<>(new ExceptionMessage(message), status);
    }

}
