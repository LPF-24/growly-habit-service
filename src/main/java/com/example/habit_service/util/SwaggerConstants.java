package com.example.habit_service.util;

public class SwaggerConstants {
    public static final String ID_DESC = "Unique identifier of the habit";
    public static final String ID_EXAMPLE = "123";

    public static final String NAME_DESC = "Name for the habit. Must be between 2 and 255 characters.";
    public static final String NAME_EXAMPLE = "Drink water";

    public static final String DESCRIPTION_DESC = "Description of the habit. Can be empty or up to 255 characters.";
    public static final String DESCRIPTION_EXAMPLE = "Drink 2L of water every day";

    public static final String ACTIVE_DESC = "Habit activity status. Cannot be null, takes the values true or false.";
    public static final String ACTIVE_EXAMPLE = "true";

    public static final String PERSON_ID_DESC = "The user ID of the user who has the habit. Cannot be empty, must have a numeric value.";
    public static final String PERSON_ID_EXAMPLE = "45";

    public static final String CREATED_AT_DESC = "Date of habit creation.";
    public static final String CREATED_AT_EXAMPLE = "2025-04-25";

    public static final String MESSAGE_DESC = "Field for any messages.";
    public static final String MESSAGE_EXAMPLE = "Habit with id 5 successfully removed.";
}
