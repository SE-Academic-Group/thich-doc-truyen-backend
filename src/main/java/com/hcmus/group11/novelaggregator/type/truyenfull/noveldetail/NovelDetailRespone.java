package com.hcmus.group11.novelaggregator.type.truyenfull.noveldetail;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NovelDetailRespone {
    private String status;
    private String messgage;
    @JsonProperty("status_code")
    private int statusCode;
    private NovelDetailData data;
}
