package com.example.habit_service.controller;

import com.example.habit_service.service.HabitEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/habits")
public class HabitTestController {
    private final HabitEventPublisher publisher;

    public HabitTestController(HabitEventPublisher publisher) {
        this.publisher = publisher;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Habit Service!";
    }

    @PostMapping("/complete/{id}")
    public ResponseEntity<String> completeHabit(@PathVariable String id) {
        publisher.publishHabitCompleted(id);
        return ResponseEntity.ok("Habit " + id + "marked as completed.");
    }
}
