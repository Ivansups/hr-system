package ru.klimov.hrsystem.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.klimov.hrsystem.exception.ValidationException;

public final class JsonMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private JsonMapper() {
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new ValidationException("Malformed JSON body: " + e.getMessage());
        }
    }
}
