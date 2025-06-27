package com.example.habit_service.service;

import com.example.habit_service.dto.UserDeletedEvent;
import com.example.habit_service.repository.HabitRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserDeletionListener {
    private final Logger logger = LoggerFactory.getLogger(UserDeletionListener.class);
    @Autowired
    private HabitRepository habitRepository;

    @PostConstruct
    public void init() {
        logger.info("✅ UserDeletionListener initialized");
    }

    @KafkaListener(topics = "user-deleted", groupId = "habit-group")
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {
        Long personId = event.getPersonId();
        logger.debug("Received delete event for personId = {}", personId);

        var habits = habitRepository.findByPersonId(personId);
        logger.debug("Found {}", habits.size() + " habits before deletion.");

        habitRepository.deleteAllByPersonId(personId);

        var after = habitRepository.findByPersonId(personId);
        logger.debug("Remaining {}", after.size() + " habits after deletion.");
    }
}
