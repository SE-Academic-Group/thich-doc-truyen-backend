package com.hcmus.group11.novelaggregator.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@AllArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {
    private T data;
    private HashMap<String, Object> metadata;

    public ApiResponse(T data) {
        this.data = data;
        this.metadata = null;
    }
}
