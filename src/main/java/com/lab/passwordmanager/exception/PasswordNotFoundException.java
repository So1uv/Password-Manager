package com.lab.passwordmanager.exception;

public class PasswordNotFoundException extends RuntimeException {
    public PasswordNotFoundException(Long id) {
        super("Password entry with id=" + id + " not found");
    }
}
