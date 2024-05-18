package com.hcmus.group11.novelaggregator.type;

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

    public ApiResponse(T data) {
        this.data = data;
        this.metadata = null;
    }
}
