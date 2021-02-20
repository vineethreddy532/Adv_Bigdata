package com.bigdata5.assignment1.exceptions;

import com.bigdata5.assignment1.constants.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RedisExceptionController {

    @ExceptionHandler(value = RedisException.class)
    public ResponseEntity<Object> exception(RedisException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(Constants.MESSAGE, exception.getMessage());

        if (Constants.BAD_REQUEST.equalsIgnoreCase(exception.getErrCode())) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}