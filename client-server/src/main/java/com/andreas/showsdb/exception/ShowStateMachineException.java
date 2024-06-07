package com.andreas.showsdb.exception;

import org.springframework.http.HttpStatus;

public class ShowStateMachineException extends ShowsDatabaseException {
    public ShowStateMachineException() {
        super();
    }

    public ShowStateMachineException(HttpStatus status) {
        super(status);
    }

    public ShowStateMachineException(String message, HttpStatus status) {
        super(message, status);
    }
}
