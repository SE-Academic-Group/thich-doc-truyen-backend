package com.hcmus.group11.novelaggregator.exception.type;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private TransformedHttpException error;

    public ErrorResponse(HttpException error) {
        this.error = new TransformedHttpException(error);
    }

    public ErrorResponse(TransformedHttpException error) {
        this.error = error;
    }
}
