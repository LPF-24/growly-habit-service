package com.example.habit_service.security;

import com.example.habit_service.repository.HabitRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("habitSecurity")
public class HabitSecurity {
    private final HabitRepository habitRepository;

    public HabitSecurity(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public boolean isOwner(Long habitId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails user = (PersonDetails) auth.getPrincipal();
        Long userId = user.getId();

        return habitRepository.findById(habitId)
                .map(h -> h.getPersonId().equals(userId))
                .orElse(false);
    }
}
