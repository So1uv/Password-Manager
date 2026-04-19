package com.lab.passwordmanager.repository;

import com.lab.passwordmanager.model.Password;
import com.lab.passwordmanager.model.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PasswordRepositoryTest {

    @Autowired
    private PasswordRepository repository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void shouldSaveAndFindByService() {
        repository.save(new Password("gmail", "user@gmail.com", "hashed1"));

        Optional<Password> result = repository.findByService("gmail");

        assertTrue(result.isPresent());
        assertEquals("user@gmail.com", result.get().getUsername());
    }

    @Test
    void shouldFindByUsernameContaining() {
        repository.save(new Password("github", "john_doe", "password123"));
        repository.save(new Password("gitlab", "johnny", "password456"));
        repository.save(new Password("twitter", "nobody", "password789"));

        List<Password> results = repository.findByUsernameContainingIgnoreCase("john");

        assertEquals(2, results.size());
    }

    @Test
    void shouldReturnEmptyWhenServiceNotFound() {
        Optional<Password> result = repository.findByService("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void shouldDeleteEntry() {
        Password saved = repository.save(new Password("test-delete", "user", "pass123"));
        Long id = saved.getId();
        repository.deleteById(id);
        repository.flush();

        assertFalse(repository.existsById(id));
    }

    @Test
    void shouldFindPasswordsByTagName() {
        Tag tag = tagRepository.save(new Tag("work"));

        Password p1 = new Password("jira", "dev1", "hashed1");
        p1.getTags().add(tag);
        repository.save(p1);

        Password p2 = new Password("slack", "dev2", "hashed2");
        p2.getTags().add(tag);
        repository.save(p2);

        Password p3 = new Password("instagram", "personal_user", "hashed3");
        repository.save(p3);

        List<Password> results = repository.findByTagName("work");

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(p -> p.getService().equals("jira")));
        assertTrue(results.stream().anyMatch(p -> p.getService().equals("slack")));
    }

    @Test
    void shouldReturnEmptyWhenNoPasswordsWithTag() {
        List<Password> results = repository.findByTagName("nonexistent-tag");

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldNotDuplicateTagInDatabase() {
        Tag tag1 = tagRepository.save(new Tag("finance"));
        Tag tag2 = tagRepository.findByName("finance").orElse(null);

        assertNotNull(tag2);
        assertEquals(tag1.getId(), tag2.getId());
        assertEquals(1, tagRepository.findAll().size());
    }
}
