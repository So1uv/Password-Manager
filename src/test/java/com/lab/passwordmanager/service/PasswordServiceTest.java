package com.lab.passwordmanager.service;

import com.lab.passwordmanager.dto.PasswordRequest;
import com.lab.passwordmanager.exception.PasswordNotFoundException;
import com.lab.passwordmanager.model.Password;
import com.lab.passwordmanager.model.Tag;
import com.lab.passwordmanager.repository.PasswordRepository;
import com.lab.passwordmanager.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordRepository repository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordService service;

    @Test
    void shouldEncryptPasswordBeforeSaving() {
        PasswordRequest req = new PasswordRequest();
        req.setService("gmail");
        req.setUsername("user");
        req.setPassword("plaintext");

        when(passwordEncoder.encode("plaintext")).thenReturn("$2a$hashed");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Password result = service.save(req);

        assertNotEquals("plaintext", result.getPassword());
        assertEquals("$2a$hashed", result.getPassword());
        verify(passwordEncoder).encode("plaintext");
    }

    @Test
    void shouldThrowWhenPasswordNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PasswordNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void shouldDeleteById() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.delete(1L));
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentEntry() {
        when(repository.existsById(42L)).thenReturn(false);

        assertThrows(PasswordNotFoundException.class, () -> service.delete(42L));
    }

    @Test
    void shouldAddTagsToPassword() {
        Password password = new Password("github", "dev", "encodedPwd");
        password.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(password));
        when(tagRepository.findByName("work")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(i -> i.getArgument(0));
        when(repository.save(any(Password.class))).thenAnswer(i -> i.getArgument(0));

        Password result = service.addTags(1L, Set.of("work"));

        assertEquals(1, result.getTags().size());
        assertTrue(result.getTags().stream().anyMatch(t -> t.getName().equals("work")));
    }

    @Test
    void shouldNotDuplicateExistingTag() {
        Tag existingTag = new Tag("personal");
        existingTag.setId(10L);

        Password password = new Password("twitter", "user1", "encodedPwd");
        password.setId(2L);

        when(repository.findById(2L)).thenReturn(Optional.of(password));
        when(tagRepository.findByName("personal")).thenReturn(Optional.of(existingTag));
        when(repository.save(any(Password.class))).thenAnswer(i -> i.getArgument(0));

        service.addTags(2L, Set.of("personal"));

        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void shouldRemoveTagsFromPassword() {
        Tag tag = new Tag("social");
        tag.setId(5L);

        Password password = new Password("facebook", "user", "encodedPwd");
        password.setId(3L);
        password.getTags().add(tag);

        when(repository.findById(3L)).thenReturn(Optional.of(password));
        when(repository.save(any(Password.class))).thenAnswer(i -> i.getArgument(0));

        Password result = service.removeTags(3L, Set.of("social"));

        assertTrue(result.getTags().isEmpty());
    }

    @Test
    void shouldSearchByTag() {
        Password p1 = new Password("github", "dev", "hash");
        Password p2 = new Password("gitlab", "dev2", "hash");

        when(repository.findByTagName("dev")).thenReturn(List.of(p1, p2));

        List<Password> results = service.searchByTag("dev");

        assertEquals(2, results.size());
        verify(repository).findByTagName("dev");
    }
}
