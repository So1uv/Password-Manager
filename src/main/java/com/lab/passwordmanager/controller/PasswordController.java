package com.lab.passwordmanager.controller;

import com.lab.passwordmanager.dto.PasswordRequest;
import com.lab.passwordmanager.model.Password;
import com.lab.passwordmanager.service.PasswordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/passwords")
public class PasswordController {

    private final PasswordService service;

    public PasswordController(PasswordService service) {
        this.service = service;
    }

    @GetMapping
    public List<Password> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Password getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    public List<Password> search(@RequestParam String q) {
        return service.searchByUsername(q);
    }

    @GetMapping("/by-tag")
    public List<Password> searchByTag(@RequestParam String tag) {
        return service.searchByTag(tag);
    }

    @PostMapping
    public ResponseEntity<Password> create(@Valid @RequestBody PasswordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }

    @PutMapping("/{id}")
    public Password update(@PathVariable Long id,
                           @Valid @RequestBody PasswordRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/tags")
    public Password addTags(@PathVariable Long id,
                            @RequestBody Set<String> tagNames) {
        return service.addTags(id, tagNames);
    }

    @PutMapping("/{id}/tags")
    public Password setTags(@PathVariable Long id,
                            @RequestBody Set<String> tagNames) {
        return service.setTags(id, tagNames);
    }

    @DeleteMapping("/{id}/tags")
    public Password removeTags(@PathVariable Long id,
                               @RequestBody Set<String> tagNames) {
        return service.removeTags(id, tagNames);
    }
}
