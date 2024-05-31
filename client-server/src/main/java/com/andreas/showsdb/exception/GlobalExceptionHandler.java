package com.andreas.showsdb.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = ShowsDatabaseException.class)
    public ResponseEntity<ExceptionMessage> handleNotFoundException(ShowsDatabaseException e) {
        return e.getResponse();
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<ExceptionMessage> handleResourceNotFound(NoResourceFoundException e) {
        String message = e.getMessage();
        logger.error(message);
        return new ResponseEntity<>(new ExceptionMessage(message), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionMessage> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = e.getMessage();
        logger.error(message);
        return ResponseEntity.badRequest().body(new ExceptionMessage(e.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionMessage> handleException(Exception e) {
        String message = e.getMessage();
        logger.error(message);
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(new ExceptionMessage(message));
    }
}
