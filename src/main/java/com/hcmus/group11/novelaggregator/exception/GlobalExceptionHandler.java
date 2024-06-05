package com.hcmus.group11.novelaggregator.exception;

import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.exception.type.TransformedHttpException;
import com.hcmus.group11.novelaggregator.type.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler
        extends ResponseEntityExceptionHandler {


    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        TransformedHttpException transformed = new TransformedHttpException("NOT_FOUND", "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL());
        return new ResponseEntity<>(new ApiResponse<Object>(null, null, transformed), HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        TransformedHttpException transformed = new TransformedHttpException("BAD_REQUEST", "Missing parameter: " + ex.getParameterName());
        return new ResponseEntity<>(new ApiResponse<Object>(null, null, transformed), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex)  {
        TransformedHttpException transformed = new TransformedHttpException("BAD_REQUEST", "Failed to convert argument: "+ ex.getName()  +" with type " + ex.getValue().getClass().getSimpleName() + " to required type " + ex.getRequiredType().getSimpleName());
        return new ResponseEntity<>(new ApiResponse<Object>(null, null, transformed), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {HttpException.class})
    protected ResponseEntity<Object> handleHttpException(HttpException ex) {
        if (ex.getStatusCode().is5xxServerError()) {
            System.out.println(ex.getCause().getMessage());
            TransformedHttpException transformed = new TransformedHttpException("INTERNAL_SERVER_ERROR", "Something went wrong");
            return new ResponseEntity<>(new ApiResponse<Object>(null, null, transformed), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(new ApiResponse<Object>(null, null, new TransformedHttpException(ex)), ex.getStatusCode());
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleException(Exception ex) {
        TransformedHttpException transformed = new TransformedHttpException("INTERNAL_SERVER_ERROR", "Something went wrong");
        return new ResponseEntity<>(new ApiResponse<Object>(null, null, transformed), HttpStatus.INTERNAL_SERVER_ERROR);
    }



}
