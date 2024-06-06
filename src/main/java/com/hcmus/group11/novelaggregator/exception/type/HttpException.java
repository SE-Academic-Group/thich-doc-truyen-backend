package com.hcmus.group11.novelaggregator.exception.type;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

@Setter
@Getter
public class HttpException extends ResponseStatusException {
    private String errorCode;

    public HttpException(HttpStatusCode status) {
        super(status);
    }

    public HttpException(HttpStatusCode status, String reason) {
        super(status, reason);
    }

    public HttpException(int rawStatusCode, String reason, Throwable cause) {
        super(rawStatusCode, reason, cause);
    }

    public HttpException(HttpStatusCode status, String reason, Throwable cause) {
        super(status, reason, cause);
    }

    public static HttpException BAD_REQUEST(String errorCode, String reason, Throwable cause) {
        HttpException ex = new HttpException(HttpStatus.BAD_REQUEST, reason, cause);
        ex.setErrorCode(errorCode);
        return ex;
    }

    public static HttpException BAD_REQUEST(String errorCode, String reason) {
        HttpException ex = new HttpException(HttpStatus.BAD_REQUEST, reason);
        ex.setErrorCode(errorCode);
        return ex;
    }

    public static HttpException INTERNAL_SERVER_ERROR(String errorCode, String reason, Throwable cause) {
        HttpException ex = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, reason, cause);
        ex.setErrorCode(errorCode);
        return ex;
    }

    public static HttpException NOT_FOUND(String errorCode, String reason, Throwable cause) {
        HttpException ex = new HttpException(HttpStatus.NOT_FOUND, reason, cause);
        ex.setErrorCode(errorCode);
        return ex;
    }

    public static HttpException NOT_FOUND(String errorCode, String reason) {
        HttpException ex = new HttpException(HttpStatus.NOT_FOUND, reason);
        ex.setErrorCode(errorCode);
        return ex;
    }

    public static HttpException UNAUTHORIZED(String errorCode, String reason, Throwable cause) {
        HttpException ex = new HttpException(HttpStatus.UNAUTHORIZED, reason, cause);
        ex.setErrorCode(errorCode);
        return ex;
    }

    public static HttpException UNAUTHORIZED(String errorCode, String reason) {
        HttpException ex = new HttpException(HttpStatus.UNAUTHORIZED, reason);
        ex.setErrorCode(errorCode);
        return ex;
    }

    public static HttpException FORBIDDEN(String errorCode, String reason, Throwable cause) {
        HttpException ex = new HttpException(HttpStatus.FORBIDDEN, reason, cause);
        ex.setErrorCode(errorCode);
        return ex;
    }

    public static HttpException FORBIDDEN(String errorCode, String reason) {
        HttpException ex = new HttpException(HttpStatus.FORBIDDEN, reason);
        ex.setErrorCode(errorCode);
        return ex;
    }

    public static HttpException SERVICE_UNAVAILABLE(String errorCode, String reason, Throwable cause) {
        HttpException ex = new HttpException(HttpStatus.SERVICE_UNAVAILABLE, reason, cause);
        ex.setErrorCode(errorCode);
        return ex;
    }

    public static HttpException SERVICE_UNAVAILABLE(String errorCode, String reason) {
        HttpException ex = new HttpException(HttpStatus.SERVICE_UNAVAILABLE, reason);
        ex.setErrorCode(errorCode);
        return ex;
    }
}
