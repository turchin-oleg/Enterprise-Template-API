package com.example.api.controllers;

import com.example.api.controllers.payload.LoginRequest;
import com.example.api.security.pwdreset.TokenPasswordReset;
import com.example.api.security.pwdreset.TokenPasswordResetRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Oleg Turchin
 */
@SpringBootTest()
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TokenPasswordResetRepository tokenPasswordResetRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    private LoginRequest loginRequest;

    @SneakyThrows
    @Test
    @Order(1)
    void authenticateUser() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");
        String body = mapper.writeValueAsString(loginRequest);
        MvcResult content = mvc.perform(post("/api/auth/signin")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();
        JsonNode rootNode = mapper.readTree(content.getResponse().getContentAsString());
        assertTrue(rootNode.has("id"));
        assertNotNull(rootNode.get("id"));
        assertTrue(rootNode.has("token"));
        assertNotNull(rootNode.get("token"));
        assertTrue(rootNode.has("username"));
        assertEquals("admin", rootNode.get("username").asText());
        assertTrue(rootNode.has("email"));
        assertTrue(rootNode.has("roles"));
        assertTrue(rootNode.get("roles").isArray());
        JsonNode roles = rootNode.get("roles");
        assertEquals("ROLE_ADMIN", roles.get(0).asText());

        loginRequest.setUsername("dummy_user");
        loginRequest.setPassword("dummyUserPassword");
        body = mapper.writeValueAsString(loginRequest);
        mvc.perform(post("/api/auth/signin")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().is4xxClientError())
                .andReturn();

    }

    @SneakyThrows
    @Test
    @Order(2)
    void resetPassword() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");
        String body = mapper.writeValueAsString(loginRequest);
        MvcResult content = mvc.perform(post("/api/auth/signin")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rootNode = mapper.readTree(content.getResponse().getContentAsString());
        assertTrue(rootNode.has("id"));
        assertNotNull(rootNode.get("id"));
        assertTrue(rootNode.has("email"));
        assertNotNull(rootNode.get("email"));

        String email = rootNode.get("email").asText();
        mvc.perform(post("/api/auth/resetPassword?email=" + email)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        email = "dummyUserEmail";
        mvc.perform(post("/api/auth/resetPassword?email=" + email)
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @SneakyThrows
    @Test
    @Order(3)
    void changePassword() {
        Iterable<TokenPasswordReset> tokenPasswordResets = tokenPasswordResetRepository.findAll();
        String token = "";
        for (TokenPasswordReset tokenPasswordReset: tokenPasswordResets) {
            if (tokenPasswordReset.getExpirationDate().getTime() > new Date().getTime()) {
                token = tokenPasswordReset.getToken();
            }
        }
        mvc.perform(get("/api/auth/changePassword?token=" + token))
                .andExpect(status().isOk())
                .andReturn();
        token = "111";
        mvc.perform(get("/api/auth/changePassword?token=" + token))
                .andExpect(status().isOk())
                .andReturn();
    }
}