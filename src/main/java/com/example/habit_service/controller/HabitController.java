package com.example.habit_service.controller;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.exception.ErrorUtil;
import com.example.habit_service.service.HabitEventPublisher;
import com.example.habit_service.service.HabitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Habit", description = "Endpoints for work with habit.")
@RestController
@RequestMapping("/habits")
public class HabitController {
    private final HabitEventPublisher publisher;
    private final HabitService habitService;

    public HabitController(HabitEventPublisher publisher, HabitService habitService) {
        this.publisher = publisher;
        this.habitService = habitService;
    }

    @Operation(summary = "Getting all habits", description = "Getting all habits for a specific person by id of this person.")
    @ApiResponse(responseCode = "200", description = "Successfully received.")
    @ApiResponse(responseCode = "400", description = "Missing required parameters of request.")
    @ApiResponse(responseCode = "403", description = "User is authenticated, but not allowed to access this resource.")
    @ApiResponse(responseCode = "500", description = "Error inside method.")
    @ApiResponse(responseCode = "503", description = "Service unavailable.")
    @ApiResponse(responseCode = "401", description = "Authentication is required or token is missing/invalid.")
    @GetMapping
    public ResponseEntity<List<HabitResponseDTO>> getAllHabits(@RequestParam Long personId) {
        List<HabitResponseDTO> habits = habitService.getAllHabitsByPersonId(personId);
        return ResponseEntity.ok(habits);
    }

    @Operation(summary = "Getting one habit.", description = "Get one habit by unique habit id.")
    @ApiResponse(responseCode = "200", description = "Successfully received.")
    @ApiResponse(responseCode = "500", description = "Error inside method.")
    @ApiResponse(responseCode = "503", description = "Service unavailable.")
    @ApiResponse(responseCode = "403", description = "User is authenticated, but not allowed to access this resource.")
    @ApiResponse(responseCode = "401", description = "Authentication is required or token is missing/invalid.")
    @GetMapping("/{id}")
    public ResponseEntity<HabitResponseDTO> getHabit(@PathVariable Long id) {
        return ResponseEntity.ok(habitService.getHabitById(id));
    }

    @Operation(summary = "Add a habit.", description = "Adds a habit of the user.")
    @ApiResponse(responseCode = "200", description = "Successfully added.")
    @ApiResponse(responseCode = "500", description = "Error inside method.")
    @ApiResponse(responseCode = "422", description = "Request is syntactically correct, but semantically invalid.")
    @ApiResponse(responseCode = "403", description = "User is authenticated, but not allowed to access this resource.")
    @ApiResponse(responseCode = "401", description = "Authentication is required or token is missing/invalid.")
    @ApiResponse(responseCode = "503", description = "Service unavailable.")
    @PostMapping
    public ResponseEntity<HabitResponseDTO> newHabit(@RequestBody @Valid HabitRequestDTO dto,
                                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            ErrorUtil.throwIfHasErrors(bindingResult);

        HabitResponseDTO response = habitService.createHabit(dto);
        publisher.publishHabitCompleted(String.valueOf(response.getId()));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a habit.", description = "Deletes a habit of the user.")
    @ApiResponse(responseCode = "200", description = "Successfully deleted.")
    @ApiResponse(responseCode = "500", description = "Error inside method.")
    @ApiResponse(responseCode = "403", description = "User is authenticated, but not allowed to access this resource.")
    @ApiResponse(responseCode = "401", description = "Authentication is required or token is missing/invalid.")
    @ApiResponse(responseCode = "503", description = "Service unavailable.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        publisher.habitDeleted(id);
        return ResponseEntity.ok("Habit with id " + id + " successfully removed.");
    }

    @Operation(summary = "Update a habit.", description = "Updates a habit of the user.")
    @ApiResponse(responseCode = "200", description = "Successfully deleted.")
    @ApiResponse(responseCode = "500", description = "Error inside method.")
    @ApiResponse(responseCode = "403", description = "User is authenticated, but not allowed to access this resource.")
    @ApiResponse(responseCode = "422", description = "Request is syntactically correct, but semantically invalid.")
    @ApiResponse(responseCode = "401", description = "Authentication is required or token is missing/invalid.")
    @ApiResponse(responseCode = "503", description = "Service unavailable.")
    @PatchMapping("/{id}")
    public ResponseEntity<HabitResponseDTO> updateHabit(@PathVariable Long id,
                                                        @RequestBody @Valid HabitRequestDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            ErrorUtil.throwIfHasErrors(bindingResult);

        publisher.habitUpdated(id);
        return ResponseEntity.ok(habitService.updateHabit(id, dto));
    }
}
