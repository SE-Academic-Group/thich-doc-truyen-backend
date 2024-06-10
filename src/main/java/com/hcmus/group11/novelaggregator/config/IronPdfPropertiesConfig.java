package com.hcmus.group11.novelaggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "ironpdf")
@Configuration
@Getter
@Setter
public class IronPdfPropertiesConfig {
    private String licenseKey;
}
