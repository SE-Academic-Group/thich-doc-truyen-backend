package com.hcmus.group11.novelaggregator.exception;

import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.type.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler
        extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {HttpException.class})
    protected ApiResponse<Object> handleHttpException(HttpException ex) {
        if (ex.getStatusCode().is5xxServerError()) {
            System.out.println(ex.getCause().getMessage());
            return new ApiResponse<>(HttpException.INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Something went wrong. Please contact the administrator.", null));
        }

        System.out.println("Not 500 Error");
        return new ApiResponse<>(ex);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ApiResponse<Object> handleException(Exception ex) {
        return new ApiResponse<>(HttpException.INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", ex.getMessage(), ex));
    }
}
