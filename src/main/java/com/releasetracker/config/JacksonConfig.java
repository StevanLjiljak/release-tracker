package com.releasetracker.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.releasetracker.enums.ReleaseStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    public Module releaseStatusModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ReleaseStatus.class, new JsonDeserializer<>() {
            @Override
            public ReleaseStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return ReleaseStatus.fromDisplayName(p.getText());
            }
        });
        return module;
    }
}
