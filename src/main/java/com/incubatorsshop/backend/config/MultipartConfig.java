package com.incubatorsshop.backend.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import jakarta.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // Set limits to 50MB
        factory.setMaxFileSize(DataSize.ofTerabytes(1));
        factory.setMaxRequestSize(DataSize.ofTerabytes(1));
        return factory.createMultipartConfig();
    }
}