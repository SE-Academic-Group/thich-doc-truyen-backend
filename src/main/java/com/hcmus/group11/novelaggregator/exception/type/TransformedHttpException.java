package com.hcmus.group11.novelaggregator.exception.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TransformedHttpException {
    private String errorCode;
    private String reason;

    public TransformedHttpException(HttpException exception) {
        this.errorCode = exception.getErrorCode();
        this.reason = exception.getReason();
    }
}
