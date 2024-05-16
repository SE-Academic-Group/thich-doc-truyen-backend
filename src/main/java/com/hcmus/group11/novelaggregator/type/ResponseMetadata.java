package com.hcmus.group11.novelaggregator.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@AllArgsConstructor
@Getter
@Setter
public class ResponseMetadata {
    private HashMap<String, Object> metadata;

    public ResponseMetadata() {
        this.metadata = new HashMap<>();
    }

    public void addMetadataValue(String key, Object data) {
        this.metadata.put(key, data);
    }

    public Object getMetadataValue(String key) {
        return this.metadata.get(key);
    }
}
