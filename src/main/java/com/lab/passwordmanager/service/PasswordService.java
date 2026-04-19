package com.lab.passwordmanager.service;

import com.lab.passwordmanager.dto.PasswordRequest;
import com.lab.passwordmanager.exception.PasswordNotFoundException;
import com.lab.passwordmanager.model.Password;
import com.lab.passwordmanager.model.Tag;
import com.lab.passwordmanager.repository.PasswordRepository;
import com.lab.passwordmanager.repository.TagRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PasswordService {

    private final PasswordRepository repository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordRepository repository,
                           TagRepository tagRepository,
                           PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.tagRepository = tagRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Password> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Password getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PasswordNotFoundException(id));
    }

    public Password save(PasswordRequest req) {
        Password entity = mapToEntity(req);
        entity.setPassword(passwordEncoder.encode(req.getPassword()));
        return repository.save(entity);
    }

    public Password update(Long id, PasswordRequest req) {
        Password existing = getById(id);
        existing.setService(req.getService());
        existing.setUsername(req.getUsername());
        existing.setNotes(req.getNotes());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new PasswordNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Password> searchByUsername(String username) {
        return repository.findByUsernameContainingIgnoreCase(username);
    }

    @Transactional(readOnly = true)
    public List<Password> searchByTag(String tagName) {
        return repository.findByTagName(tagName);
    }

    public Password addTags(Long passwordId, Set<String> tagNames) {
        Password password = getById(passwordId);
        Set<Tag> tags = resolveTags(tagNames);
        password.getTags().addAll(tags);
        return repository.save(password);
    }

    public Password removeTags(Long passwordId, Set<String> tagNames) {
        Password password = getById(passwordId);
        password.getTags().removeIf(tag -> tagNames.contains(tag.getName()));
        return repository.save(password);
    }

    public Password setTags(Long passwordId, Set<String> tagNames) {
        Password password = getById(passwordId);
        password.getTags().clear();
        password.getTags().addAll(resolveTags(tagNames));
        return repository.save(password);
    }

    private Set<Tag> resolveTags(Set<String> names) {
        return names.stream()
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toSet());
    }

    private Password mapToEntity(PasswordRequest req) {
        Password p = new Password();
        p.setService(req.getService());
        p.setUsername(req.getUsername());
        p.setNotes(req.getNotes());
        return p;
    }
}
