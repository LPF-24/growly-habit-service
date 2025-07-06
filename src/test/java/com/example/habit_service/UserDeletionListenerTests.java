package com.example.habit_service;

import com.example.habit_service.dto.UserDeletedEvent;
import com.example.habit_service.entity.Habit;
import com.example.habit_service.repository.HabitRepository;
import com.example.habit_service.service.UserDeletionListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDeletionListenerTests {
    @Mock
    private HabitRepository habitRepository;

    @InjectMocks
    private UserDeletionListener userDeletionListener;

    @Test
    void shouldHandleEventAndDeleteHabits() {
        Long personId = 1L;
        UserDeletedEvent event = new UserDeletedEvent();
        event.setPersonId(personId);

        Habit habit1 = new Habit();
        habit1.setId(1L);
        habit1.setPersonId(personId);

        Habit habit2 = new Habit();
        habit2.setId(2L);
        habit2.setPersonId(personId);

        when(habitRepository.findByPersonId(personId)).thenReturn(List.of(habit1, habit2)).thenReturn(List.of());

        userDeletionListener.handleUserDeleted(event);

        verify(habitRepository, times(2)).findByPersonId(personId);
        verify(habitRepository).deleteAllByPersonId(personId);
    }
}
