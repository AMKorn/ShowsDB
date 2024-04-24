package com.andreas.showsdb.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ShowsDatabaseException.class)
    public ResponseEntity<ExceptionMessage> handleNotFoundException(ShowsDatabaseException e) {
        return e.getResponse();
    }

    @ExceptionHandler(value = Exception.class)
    public void handleException(Exception e) {
        System.err.println(e.getMessage());
    }
}
