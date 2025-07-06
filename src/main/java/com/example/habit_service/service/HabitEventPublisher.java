package com.example.habit_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class HabitEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Logger logger = LoggerFactory.getLogger(HabitEventPublisher.class);

    public HabitEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishHabitCompleted(Long habitId) {
        String message = "Habit completed: " + habitId;
        kafkaTemplate.send("habit-events", message);
        logger.info("Sent to Kafka: {}", message);
    }

    public void habitDeleted(Long habitId) {
        String message = "Habit deleted: " + habitId;
        kafkaTemplate.send("habit-events", message);
        logger.info("Sent to Kafka: {}", message);
    }

    public void habitUpdated(Long habitId) {
        String message = "Habit updated: " + habitId;
        kafkaTemplate.send("habit-events", message);
        logger.info("Sent to Kafka: {}", message);
    }
}
