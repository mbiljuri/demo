package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class NoCryptoValuesAdvice {

    @ResponseBody
    @ExceptionHandler(NoCryptoValuesException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String noCryptoValuesHandler(NoCryptoValuesException e) {
        return e.getMessage();
    }
}
