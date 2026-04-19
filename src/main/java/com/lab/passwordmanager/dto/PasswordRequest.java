package com.lab.passwordmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordRequest {

    @NotBlank(message = "Service must not be blank")
    private String service;

    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;

    private String notes;

    public String getService()           { return service; }
    public void setService(String s)     { this.service = s; }
    public String getUsername()          { return username; }
    public void setUsername(String u)    { this.username = u; }
    public String getPassword()          { return password; }
    public void setPassword(String p)    { this.password = p; }
    public String getNotes()             { return notes; }
    public void setNotes(String n)       { this.notes = n; }
}
