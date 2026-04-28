package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.config.DynamoDbTestConfig;
import com.garygz.dspdemoproject.repository.AdvertiserRepository;
import com.garygz.dspdemoproject.repository.CampaignRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared base for controller integration tests.
 *
 * Uses H2 (configured in src/test/resources/application.properties) and
 * mock DynamoDB beans (DynamoDbTestConfig) so no AWS connectivity is needed.
 *
 * Cleans JPA tables after every test and exposes a helper to obtain a
 * dev-login JWT for authenticated requests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(DynamoDbTestConfig.class)
public abstract class BaseControllerIT {

    @Autowired private WebApplicationContext wac;
    @Autowired private AdvertiserRepository advertiserRepository;
    @Autowired private CampaignRepository campaignRepository;

    protected MockMvc mockMvc;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    void cleanDb() {
        campaignRepository.deleteAll();
        advertiserRepository.deleteAll();
    }

    /** Returns a Bearer token obtained via the dev-login shortcut. */
    protected String devToken() throws Exception {
        String body = mockMvc.perform(get("/auth/dev-login"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(body).get("token").asText();
    }
}
