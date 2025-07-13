package com.example.habit_service.integration.exception;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.yml")
public class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldHandleTypeMismatchException() throws Exception {
        mockMvc.perform(delete("/test-errors/mismatch/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value 'abc' for parameter 'id'"))
                .andExpect(jsonPath("$.path").value("/test-errors/mismatch/abc"));
    }

    @Test
    void shouldHandleEntityNotFoundException() throws Exception {
        mockMvc.perform(get("/test-errors/entity-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("error: User not found"))
                .andExpect(jsonPath("$.path").value("/test-errors/entity-not-found"));
    }

    @Test
    void shouldHandleAnyUncheckedException() throws Exception {
        mockMvc.perform(get("/test-errors/runtime-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }

    @Test
    void shouldHandleValidationException_whenInvalidInput() throws Exception {
        mockMvc.perform(post("/create-habit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {
                                    "name": "M",
                                    "active": true
                                 }
                                 """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("name - Product name must be between 2 and 255 characters;")));
    }

    @Test
    void shouldHandleHttpMessageNotReadableException_whenJSONIsEmpty() throws Exception {
        mockMvc.perform(post("/create-habit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or missing request body"));
    }

    @Test
    void shouldHandleAuthorizationDeniedException_whenUserIsUnauthorized() throws Exception {
        mockMvc.perform(delete("/test-errors/trigger-auth-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied for testing"))
                .andExpect(jsonPath("$.path").value("/test-errors/trigger-auth-denied"));
    }

    @Test
    void shouldHandleBadRequestException() throws Exception {
        mockMvc.perform(get("/test-errors/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad input provided"));
    }

    @TestConfiguration
    public static class TestKafkaConfig {

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, String> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();

            factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(testConsumerConfigs()));
            factory.setAutoStartup(false); // 💡 Отключаем автоматический запуск

            return factory;
        }

        private Map<String, Object> testConsumerConfigs() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // dummy
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            return props;
        }
    }
}
