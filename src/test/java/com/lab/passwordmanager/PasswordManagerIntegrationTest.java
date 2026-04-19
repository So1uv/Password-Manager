package com.lab.passwordmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.passwordmanager.dto.PasswordRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test — loads the complete Spring Boot context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PasswordManagerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void fullCrudFlow() throws Exception {
        // CREATE
        PasswordRequest req = new PasswordRequest();
        req.setService("instagram");
        req.setUsername("photo_user");
        req.setPassword("secure1234");

        String body = mockMvc.perform(post("/api/passwords")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(body).get("id").asLong();

        // READ
        mockMvc.perform(get("/api/passwords/" + id))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.service").value("instagram"));

        // UPDATE
        req.setUsername("photo_user_v2");
        mockMvc.perform(put("/api/passwords/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.username").value("photo_user_v2"));

        // DELETE
        mockMvc.perform(delete("/api/passwords/" + id))
               .andExpect(status().isNoContent());

        // 404 after delete
        mockMvc.perform(get("/api/passwords/" + id))
               .andExpect(status().isNotFound());
    }
}
