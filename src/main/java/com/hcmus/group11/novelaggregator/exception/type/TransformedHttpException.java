package com.hcmus.group11.novelaggregator.exception.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "HTTP Exception Response")
public class TransformedHttpException {
    @Schema(description = "Error code of the exception", example = "ERROR_CODE")
    private String errorCode;
    @Schema(description = "Reason of the exception", example = "Reason of the exception")
    private String reason;

    public TransformedHttpException(HttpException exception) {
        this.errorCode = exception.getErrorCode();
        this.reason = exception.getReason();
    }
}
