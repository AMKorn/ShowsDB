package com.andreas.showsdb.batch;

import com.andreas.showsdb.exception.ShowsDatabaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BatchProcessingException extends ShowsDatabaseException {
    public BatchProcessingException(HttpStatus status, String message) {
        super(message, status);
    }
}
