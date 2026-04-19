package com.lab.passwordmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "passwords")
public class Password {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Service name must not be blank")
    @Column(nullable = false)
    private String service;

    @NotBlank(message = "Username must not be blank")
    @Column(nullable = false)
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 4, message = "Password must be at least 4 characters")
    @Column(nullable = false)
    private String password;

    @Column(length = 512)
    private String notes;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "password_tags",
        joinColumns = @JoinColumn(name = "password_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties("passwords")
    private Set<Tag> tags = new HashSet<>();

    public Password() {}

    public Password(String service, String username, String password) {
        this.service = service;
        this.username = username;
        this.password = password;
    }

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }
    public String getService()           { return service; }
    public void setService(String s)     { this.service = s; }
    public String getUsername()          { return username; }
    public void setUsername(String u)    { this.username = u; }
    public String getPassword()          { return password; }
    public void setPassword(String p)    { this.password = p; }
    public String getNotes()             { return notes; }
    public void setNotes(String n)       { this.notes = n; }
    public Set<Tag> getTags()            { return tags; }
    public void setTags(Set<Tag> tags)   { this.tags = tags; }
}
