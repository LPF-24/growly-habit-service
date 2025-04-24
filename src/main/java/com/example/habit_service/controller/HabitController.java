package com.example.habit_service.controller;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.exception.ErrorUtil;
import com.example.habit_service.service.HabitEventPublisher;
import com.example.habit_service.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/habits")
public class HabitController {
    private final HabitEventPublisher publisher;
    private final HabitService habitService;

    public HabitController(HabitEventPublisher publisher, HabitService habitService) {
        this.publisher = publisher;
        this.habitService = habitService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Habit Service!";
    }

    @GetMapping
    public ResponseEntity<List<HabitResponseDTO>> getAllHabits(@RequestParam Long personId) {
        List<HabitResponseDTO> habits = habitService.getAllHabitsByPersonId(personId);
        return ResponseEntity.ok(habits);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitResponseDTO> getHabit(@PathVariable Long id) {
        return ResponseEntity.ok(habitService.getHabitById(id));
    }

    @PostMapping
    public ResponseEntity<HabitResponseDTO> newHabit(@RequestBody @Valid HabitRequestDTO dto,
                                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            ErrorUtil.throwIfHasErrors(bindingResult);

        return ResponseEntity.ok(habitService.createHabit(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ResponseEntity.ok("Habit with id " + id + " successfully removed.");
    }

    @PostMapping("/complete/{id}")
    public ResponseEntity<String> completeHabit(@PathVariable String id) {
        publisher.publishHabitCompleted(id);
        return ResponseEntity.ok("Habit " + id + "marked as completed.");
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<HabitResponseDTO> updateHabit(@PathVariable Long id,
                                                        @RequestBody @Valid HabitRequestDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            ErrorUtil.throwIfHasErrors(bindingResult);

        return ResponseEntity.ok(habitService.updateHabit(id, dto));
    }
}
