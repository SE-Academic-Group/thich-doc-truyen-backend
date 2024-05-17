package com.hcmus.group11.novelaggregator.type.truyenfull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcmus.group11.novelaggregator.type.truyenfull.search.Meta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private String status;
    private String message;
    @JsonProperty("status_code")
    private int statusCode;
    private Meta meta;
    private List<T> data;
}
