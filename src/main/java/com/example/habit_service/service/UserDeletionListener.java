package com.example.habit_service.service;

import com.example.habit_service.dto.UserDeletedEvent;
import com.example.habit_service.repository.HabitRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserDeletionListener {
    @Autowired
    private HabitRepository habitRepository;

    @PostConstruct
    public void init() {
        System.out.println("✅ UserDeletionListener initialized");
    }

    @KafkaListener(topics = "user-deleted", groupId = "habit-group")
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {
        Long personId = event.getPersonId();
        System.out.println("Received delete event for personId = " + personId);

        var habits = habitRepository.findByPersonId(personId);
        System.out.println("Found " + habits.size() + " habits before deletion.");

        habitRepository.deleteAllByPersonId(personId);

        var after = habitRepository.findByPersonId(personId);
        System.out.println("Remaining " + after.size() + " habits after deletion.");
    }
}
