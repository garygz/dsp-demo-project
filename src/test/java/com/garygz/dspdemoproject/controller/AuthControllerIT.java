package com.garygz.dspdemoproject.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIT extends BaseControllerIT {

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@test.com","password":"DspDemoTest!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@test.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nobody@example.com","password":"DspDemoTest!"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devLogin_enabled_returns200WithToken() throws Exception {
        mockMvc.perform(get("/auth/dev-login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void protectedEndpoint_withoutToken_isForbidden() throws Exception {
        // Spring Security 7 returns 403 (no custom entry point configured)
        mockMvc.perform(get("/advertisers"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withInvalidToken_isForbidden() throws Exception {
        mockMvc.perform(get("/advertisers")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isForbidden());
    }
}
