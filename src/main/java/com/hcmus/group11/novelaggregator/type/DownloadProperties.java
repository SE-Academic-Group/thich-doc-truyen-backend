package com.hcmus.group11.novelaggregator.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "download")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DownloadProperties {
    private List<String> types;
    private Map<String, String> paths;
    private Map<String, String> descriptions;

    @Bean
    public List<DownloadOptions> getDownloadOptionList() {
        List<DownloadOptions> result = new ArrayList<>();

        for(String type : types) {
            DownloadOptions downloadOptions = new DownloadOptions();
            downloadOptions.setType(type);
            downloadOptions.setPath(paths.get(type));
            downloadOptions.setDescription(descriptions.get(type));
            result.add(downloadOptions);
        }

        return result;
    }
}
