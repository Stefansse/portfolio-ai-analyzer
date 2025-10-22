package com.example.userservice.model.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("User with email '" + email + "' not found");
    }
}