package com.example.habit_service.unit;

import com.example.habit_service.service.HabitEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HabitEventPublisherTests {
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private HabitEventPublisher publisher;

    private Long habitId;

    @BeforeEach
    void setUp() {
        habitId = 1L;
    }

    @Test
    void shouldSendEvent_whenHabitCreated() {
        publisher.publishHabitCompleted(habitId);
        verify(kafkaTemplate).send(eq("habit-events"), eq("Habit completed: 1"));
    }

    @Test
    void shouldSendEvent_whenHabitDeleted() {
        publisher.habitDeleted(habitId);
        verify(kafkaTemplate).send(eq("habit-events"), eq("Habit deleted: 1"));
    }

    @Test
    void shouldSendEvent_whenHabitUpdated() {
        publisher.habitUpdated(habitId);
        verify(kafkaTemplate).send(eq("habit-events"), eq("Habit updated: 1"));
    }
}
