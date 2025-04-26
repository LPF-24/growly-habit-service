package com.example.habit_service.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class HabitEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public HabitEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishHabitCompleted(String habitId) {
        String message = "Habit completed: " + habitId;
        kafkaTemplate.send("habit-events", message);
        System.out.println("Отправлено в Kafka: " + message);
    }

    public void habitDeleted(Long habitId) {
        String message = "Habit deleted: " + habitId;
        kafkaTemplate.send("habit-events", message);
        System.out.println("Отправлено в Kafka: " + message);
    }

    public void habitUpdated(Long habitId) {
        String message = "Habit updated: " + habitId;
        kafkaTemplate.send("habit-events", message);
        System.out.println("Отправлено в Kafka: " + message);
    }
}
