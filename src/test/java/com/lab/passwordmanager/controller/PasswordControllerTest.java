package com.lab.passwordmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.passwordmanager.dto.PasswordRequest;
import com.lab.passwordmanager.model.Password;
import com.lab.passwordmanager.model.Tag;
import com.lab.passwordmanager.service.PasswordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import com.lab.passwordmanager.config.SecurityConfig;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PasswordController.class)
@Import(SecurityConfig.class)
class PasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PasswordService passwordService;

    @Test
    @WithMockUser
    void shouldReturnAllPasswords() throws Exception {
        when(passwordService.getAll())
            .thenReturn(List.of(new Password("gmail", "user@gmail.com", "hashed")));

        mockMvc.perform(get("/api/passwords"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].service").value("gmail"));
    }

    @Test
    @WithMockUser
    void shouldReturnPasswordById() throws Exception {
        Password p = new Password("github", "dev", "hashed");
        when(passwordService.getById(1L)).thenReturn(p);

        mockMvc.perform(get("/api/passwords/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.service").value("github"));
    }

    @Test
    @WithMockUser
    void shouldCreatePassword() throws Exception {
        PasswordRequest req = new PasswordRequest();
        req.setService("twitter");
        req.setUsername("user");
        req.setPassword("pass1234");

        Password saved = new Password("twitter", "user", "hashed");
        when(passwordService.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/passwords")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.service").value("twitter"));
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenRequestBodyInvalid() throws Exception {
        PasswordRequest req = new PasswordRequest();

        mockMvc.perform(post("/api/passwords")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/passwords"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldAddTagsToPassword() throws Exception {
        Tag tag = new Tag("work");
        Password password = new Password("jira", "dev", "hashed");
        password.getTags().add(tag);

        when(passwordService.addTags(eq(1L), any())).thenReturn(password);

        mockMvc.perform(post("/api/passwords/1/tags")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(Set.of("work"))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.tags[0].name").value("work"));
    }

    @Test
    @WithMockUser
    void shouldSearchByTag() throws Exception {
        Password p1 = new Password("jira", "dev1", "hash1");
        Password p2 = new Password("slack", "dev2", "hash2");

        when(passwordService.searchByTag("work")).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/passwords/by-tag").param("tag", "work"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void shouldRemoveTagsFromPassword() throws Exception {
        Password password = new Password("jira", "dev", "hashed");

        when(passwordService.removeTags(eq(1L), any())).thenReturn(password);

        mockMvc.perform(delete("/api/passwords/1/tags")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(Set.of("work"))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.tags").isEmpty());
    }
}
