package com.andreas.showsdb.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = ShowsDatabaseException.class)
    public ResponseEntity<ExceptionMessage> handleNotFoundException(ShowsDatabaseException e) {
        return e.getResponse();
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionMessage> handleException(Exception e) {
        String message = e.getMessage();
        logger.error(message);
        return new ResponseEntity<>(new ExceptionMessage(message), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
