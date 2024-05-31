package com.hcmus.group11.novelaggregator.exception;

import com.hcmus.group11.novelaggregator.exception.type.ErrorResponse;
import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.exception.type.TransformedHttpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler
        extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {HttpException.class})
    protected ResponseEntity<Object> handleHttpException(HttpException ex) {
        if (ex.getStatusCode().is5xxServerError()) {
            System.out.println(ex.getCause().getMessage());
            TransformedHttpException transformed = new TransformedHttpException("INTERNAL_SERVER_ERROR", "Something went wrong");
            return new ResponseEntity<>(new ErrorResponse(transformed), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(new ErrorResponse(new TransformedHttpException(ex)), ex.getStatusCode());
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleException(Exception ex) {
        TransformedHttpException transformed = new TransformedHttpException("INTERNAL_SERVER_ERROR", "Something went wrong");
        return new ResponseEntity<>(new ErrorResponse(transformed), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
