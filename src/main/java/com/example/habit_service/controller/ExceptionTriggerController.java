package com.example.habit_service.controller;

import com.example.habit_service.exception.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-errors")
public class ExceptionTriggerController {

    @DeleteMapping("/mismatch/{id}")
    public void triggerTypeMismatch(@PathVariable Long id) {
        // ничего не делать — типизация уже приведет к ошибке
    }

    @GetMapping("/entity-not-found")
    public void triggerEntityNotFound() {
        throw new EntityNotFoundException("User not found");
    }

    @GetMapping("/constraint/{id}")
    public void triggerConstraintViolation(@PathVariable @Min(5) Long id) {
    }

    @GetMapping("/bad-request")
    public void triggerBadRequest() {
        throw new BadRequestException("Bad input provided");
    }

    @GetMapping("/runtime-exception")
    public void triggeredRuntimeException() {
        throw new RuntimeException("Unexpected error");
    }

    @DeleteMapping("/trigger-auth-denied")
    public void triggerAuthDenied() {
        throw new AuthorizationDeniedException("Access denied for testing");
    }
}

