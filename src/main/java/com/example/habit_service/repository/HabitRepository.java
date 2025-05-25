package com.example.habit_service.repository;

import com.example.habit_service.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByPersonId(long personId);
    void deleteAllByPersonId(Long personId);
}
