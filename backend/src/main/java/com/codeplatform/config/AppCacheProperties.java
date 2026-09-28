package com.codeplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.cache")
@Data
public class AppCacheProperties {
    private long snippetTtlSeconds;
    private long snippetListTtlSeconds;
}
