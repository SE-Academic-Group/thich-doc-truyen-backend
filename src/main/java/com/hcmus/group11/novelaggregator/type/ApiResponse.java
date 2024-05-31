package com.hcmus.group11.novelaggregator.type;

import com.hcmus.group11.novelaggregator.exception.type.TransformedHttpException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "API response")
public class ApiResponse<T> {
    @Schema(description = "Data of the response")
    private T data;

    @Schema(description = "Metadata of the response", example = "{\"key\": \"value\"}")
    private HashMap<String, Object> metadata;

    @Schema(description = "Error of the response", example = "{\"errorCode\": \"NOVEL_NOT_FOUND\", \"reason\": \"Novel not found\"}")
    private TransformedHttpException error;

    public ApiResponse(T data) {
        this.data = data;
        this.metadata = null;
        this.error = null;
    }
}
