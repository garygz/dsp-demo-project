package com.garygz.dspdemoproject.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdvertiserControllerIT extends BaseControllerIT {

    @Test
    void listAll_returnsEmptyListWhenNoneExist() throws Exception {
        mockMvc.perform(get("/advertisers")
                        .header("Authorization", devToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void add_validAdvertiser_returns201() throws Exception {
        mockMvc.perform(post("/advertisers")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Beta Innovations"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void add_thenList_returnsCreatedAdvertiser() throws Exception {
        String token = devToken();

        mockMvc.perform(post("/advertisers")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Beta Innovations"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/advertisers")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Beta Innovations"));
    }

    @Test
    void add_blankName_returns400() throws Exception {
        mockMvc.perform(post("/advertisers")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_nameTooShort_returns400() throws Exception {
        mockMvc.perform(post("/advertisers")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Hi"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_duplicateName_returns400() throws Exception {
        String token = devToken();
        String body = """
                {"name":"Beta Innovations"}
                """;

        mockMvc.perform(post("/advertisers")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/advertisers")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
