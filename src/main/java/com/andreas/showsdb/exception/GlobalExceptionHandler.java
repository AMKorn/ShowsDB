package com.andreas.showsdb.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ShowsDatabaseException.class)
    public ResponseEntity<ExceptionMessage> handleNotFoundException(ShowsDatabaseException e) {
        return e.getResponse();
    }

    @ExceptionHandler(value = AccessDeniedException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Void> handleAccessDeniedException() {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        // No need to do anything but return the UNAUTHORIZED response status.
    }

    @ExceptionHandler(value = Exception.class)
    public void handleException(Exception e) {
        System.err.println(e.getMessage());
    }
}
