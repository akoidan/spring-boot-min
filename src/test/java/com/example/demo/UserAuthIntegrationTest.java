package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void signup_returnsJwt_and_me_requiresJwt_and_returnsUser() throws Exception {
        String signupBody = objectMapper.writeValueAsString(new Signup("alice@example.com", "Alice", "Wonder", "password123"));

        String token = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(isEmptyString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String jwt = objectMapper.readTree(token).get("token").asText();

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Wonder"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    private record Signup(String email, String firstName, String lastName, String password) {
    }
}
