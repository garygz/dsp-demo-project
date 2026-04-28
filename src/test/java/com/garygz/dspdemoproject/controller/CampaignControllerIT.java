package com.garygz.dspdemoproject.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CampaignControllerIT extends BaseControllerIT {

    private String token;
    private String advertiserId;

    @BeforeEach
    void createAdvertiser() throws Exception {
        token = devToken();

        // Create an advertiser to own the campaigns
        mockMvc.perform(post("/advertisers")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Beta Innovations"}
                                """))
                .andExpect(status().isCreated());

        // Retrieve its generated ID
        String listBody = mockMvc.perform(get("/advertisers")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        advertiserId = objectMapper.readTree(listBody).get(0).get("id").asText();
    }

    @Test
    void listCampaigns_returnsEmptyInitially() throws Exception {
        mockMvc.perform(get("/advertisers/{advId}/campaigns", advertiserId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addCampaign_validPayload_returns201() throws Exception {
        mockMvc.perform(post("/advertisers/{advId}/campaigns", advertiserId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Summer Sale 2025","landingPage":"https://acme.com/summer"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void addCampaign_thenList_returnsCampaign() throws Exception {
        mockMvc.perform(post("/advertisers/{advId}/campaigns", advertiserId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Summer Sale 2025","landingPage":"https://acme.com/summer"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/advertisers/{advId}/campaigns", advertiserId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Summer Sale 2025"));
    }

    @Test
    void addCampaign_blankName_returns400() throws Exception {
        mockMvc.perform(post("/advertisers/{advId}/campaigns", advertiserId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","landingPage":"https://acme.com"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCampaign_nameTooShort_returns400() throws Exception {
        mockMvc.perform(post("/advertisers/{advId}/campaigns", advertiserId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ads","landingPage":"https://acme.com"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCampaign_duplicateName_returns400() throws Exception {
        String body = """
                {"name":"Summer Sale 2025","landingPage":"https://acme.com/summer"}
                """;

        mockMvc.perform(post("/advertisers/{advId}/campaigns", advertiserId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/advertisers/{advId}/campaigns", advertiserId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCampaignStats_validCampaign_returns200WithEmptyData() throws Exception {
        // Create campaign
        mockMvc.perform(post("/advertisers/{advId}/campaigns", advertiserId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Summer Sale 2025","landingPage":"https://acme.com/summer"}
                                """))
                .andExpect(status().isCreated());

        // Get its ID
        String campaigns = mockMvc.perform(get("/advertisers/{advId}/campaigns", advertiserId)
                        .header("Authorization", token))
                .andReturn().getResponse().getContentAsString();

        String campaignId = objectMapper.readTree(campaigns).get(0).get("id").asText();

        mockMvc.perform(get("/advertisers/{advId}/campaigns/{campId}/stats",
                        advertiserId, campaignId)
                        .header("Authorization", token)
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.impressionsPerDay").isArray())
                .andExpect(jsonPath("$.clicksPerDay").isArray());
    }
}
