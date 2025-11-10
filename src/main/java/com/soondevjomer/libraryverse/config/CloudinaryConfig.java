package com.soondevjomer.libraryverse.config;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:#{null}}")
    private final String cloudName;

    @Value("${cloudinary.api-key:#{null}}")
    private final String apiKey;

    @Value("${cloudinary.api-secret:#{null}}")
    private final String apiSecret;

    @Bean
    public Cloudinary cloudinary() {

        log.info("config are {}, {}, {}", cloudName, apiKey, apiSecret);

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }

}
