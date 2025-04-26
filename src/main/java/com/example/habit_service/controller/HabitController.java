package com.example.habit_service.controller;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.exception.ErrorResponseDTO;
import com.example.habit_service.exception.ErrorUtil;
import com.example.habit_service.service.HabitEventPublisher;
import com.example.habit_service.service.HabitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Habit", description = "Endpoints for habit management.")
@RestController
@RequestMapping("/habits")
public class HabitController {

    private final HabitEventPublisher publisher;
    private final HabitService habitService;

    public HabitController(HabitEventPublisher publisher, HabitService habitService) {
        this.publisher = publisher;
        this.habitService = habitService;
    }

    @Operation(summary = "Get all habits", description = "Returns all habits for a specific person.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of habits",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = HabitResponseDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Missing required parameters.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    @GetMapping
    public ResponseEntity<List<HabitResponseDTO>> getAllHabits(@RequestParam Long personId) {
        List<HabitResponseDTO> habits = habitService.getAllHabitsByPersonId(personId);
        return ResponseEntity.ok(habits);
    }

    @Operation(summary = "Get a habit", description = "Returns a habit by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Habit found",
                            content = @Content(schema = @Schema(implementation = HabitResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Habit not found.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<HabitResponseDTO> getHabit(@PathVariable Long id) {
        return ResponseEntity.ok(habitService.getHabitById(id));
    }

    @Operation(summary = "Create a habit", description = "Creates a new habit.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Habit successfully created.",
                            content = @Content(schema = @Schema(implementation = HabitResponseDTO.class))),
                    @ApiResponse(responseCode = "422", description = "Validation error.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    @PostMapping
    public ResponseEntity<HabitResponseDTO> newHabit(@RequestBody @Valid HabitRequestDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            ErrorUtil.throwIfHasErrors(bindingResult);

        HabitResponseDTO response = habitService.createHabit(dto);
        publisher.publishHabitCompleted(String.valueOf(response.getId()));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a habit", description = "Deletes a habit by ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Habit successfully deleted."),
                    @ApiResponse(responseCode = "403", description = "Forbidden.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        publisher.habitDeleted(id);
        return ResponseEntity.ok("Habit with id " + id + " successfully removed.");
    }

    @Operation(summary = "Update a habit", description = "Updates a habit by ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Habit successfully updated.",
                            content = @Content(schema = @Schema(implementation = HabitResponseDTO.class))),
                    @ApiResponse(responseCode = "422", description = "Validation error.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    @PatchMapping("/{id}")
    public ResponseEntity<HabitResponseDTO> updateHabit(@PathVariable Long id,
                                                        @RequestBody @Valid HabitRequestDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            ErrorUtil.throwIfHasErrors(bindingResult);

        publisher.habitUpdated(id);
        return ResponseEntity.ok(habitService.updateHabit(id, dto));
    }
}
