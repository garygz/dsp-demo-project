package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.service.DataIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for DataIngestionController.
 *
 * DataIngestionService is replaced with a @MockBean so tests verify what the
 * controller passes to the service without touching DynamoDB at all.
 */
class DataIngestionControllerIT extends BaseControllerIT {

    @MockitoBean
    DataIngestionService dataIngestionService;

    private static final UUID CAMPAIGN_ID   = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID IMPRESSION_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID CLICK_ID      = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    // ── POST /impressions ────────────────────────────────────────────────────

    @Test
    void addImpression_validPayload_returns201() throws Exception {
        mockMvc.perform(post("/impressions")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","campaignId":"%s"}
                                """.formatted(IMPRESSION_ID, CAMPAIGN_ID)))
                .andExpect(status().isCreated());
    }

    @Test
    void addImpression_passesCorrectFieldsToService() throws Exception {
        mockMvc.perform(post("/impressions")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","campaignId":"%s"}
                                """.formatted(IMPRESSION_ID, CAMPAIGN_ID)));

        verify(dataIngestionService).addImpression(argThat(imp ->
                imp.getId().equals(IMPRESSION_ID.toString()) &&
                imp.getCampaignId().equals(CAMPAIGN_ID.toString()) &&
                imp.getTimestamp() != null
        ));
    }

    @Test
    void addImpression_invalidJson_returns400() throws Exception {
        mockMvc.perform(post("/impressions")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dataIngestionService);
    }

    @Test
    void addImpression_withoutAuth_isForbidden() throws Exception {
        mockMvc.perform(post("/impressions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","campaignId":"%s"}
                                """.formatted(IMPRESSION_ID, CAMPAIGN_ID)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dataIngestionService);
    }

    // ── POST /clicks ─────────────────────────────────────────────────────────

    @Test
    void addClick_validPayload_returns201() throws Exception {
        mockMvc.perform(post("/clicks")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","impressionId":"%s","campaignId":"%s"}
                                """.formatted(CLICK_ID, IMPRESSION_ID, CAMPAIGN_ID)))
                .andExpect(status().isCreated());
    }

    @Test
    void addClick_passesCorrectFieldsToService() throws Exception {
        mockMvc.perform(post("/clicks")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","impressionId":"%s","campaignId":"%s"}
                                """.formatted(CLICK_ID, IMPRESSION_ID, CAMPAIGN_ID)));

        verify(dataIngestionService).addClick(argThat(click ->
                click.getId().equals(CLICK_ID.toString()) &&
                click.getImpressionId().equals(IMPRESSION_ID.toString()) &&
                click.getCampaignId().equals(CAMPAIGN_ID.toString()) &&
                click.getTimestamp() != null
        ));
    }

    @Test
    void addClick_withoutAuth_isForbidden() throws Exception {
        mockMvc.perform(post("/clicks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","impressionId":"%s","campaignId":"%s"}
                                """.formatted(CLICK_ID, IMPRESSION_ID, CAMPAIGN_ID)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dataIngestionService);
    }

    // ── POST /impressions/batch ───────────────────────────────────────────────

    @Test
    void addImpressions_batch_returns201() throws Exception {
        mockMvc.perform(post("/impressions/batch")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"id":"11111111-1111-1111-1111-111111111111","campaignId":"%s"},
                                  {"id":"22222222-2222-2222-2222-222222222222","campaignId":"%s"},
                                  {"id":"33333333-3333-3333-3333-333333333333","campaignId":"%s"}
                                ]
                                """.formatted(CAMPAIGN_ID, CAMPAIGN_ID, CAMPAIGN_ID)))
                .andExpect(status().isCreated());
    }

    @Test
    void addImpressions_batch_callsServiceOnceWithAllItems() throws Exception {
        mockMvc.perform(post("/impressions/batch")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"id":"11111111-1111-1111-1111-111111111111","campaignId":"%s"},
                                  {"id":"22222222-2222-2222-2222-222222222222","campaignId":"%s"},
                                  {"id":"33333333-3333-3333-3333-333333333333","campaignId":"%s"}
                                ]
                                """.formatted(CAMPAIGN_ID, CAMPAIGN_ID, CAMPAIGN_ID)));

        // Service called once with the full list — not three separate single calls
        verify(dataIngestionService, times(1)).addImpressions(argThat(list -> list.size() == 3));
        verify(dataIngestionService, never()).addImpression(any());
    }

    @Test
    void addImpressions_emptyBatch_returns201() throws Exception {
        mockMvc.perform(post("/impressions/batch")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isCreated());

        verify(dataIngestionService).addImpressions(argThat(list -> list.isEmpty()));
    }

    // ── POST /clicks/batch ───────────────────────────────────────────────────

    @Test
    void addClicks_batch_returns201() throws Exception {
        mockMvc.perform(post("/clicks/batch")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"id":"11111111-1111-1111-1111-111111111111","impressionId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa","campaignId":"%s"},
                                  {"id":"22222222-2222-2222-2222-222222222222","impressionId":"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb","campaignId":"%s"}
                                ]
                                """.formatted(CAMPAIGN_ID, CAMPAIGN_ID)))
                .andExpect(status().isCreated());
    }

    @Test
    void addClicks_batch_callsServiceForEachItem() throws Exception {
        mockMvc.perform(post("/clicks/batch")
                        .header("Authorization", devToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"id":"11111111-1111-1111-1111-111111111111","impressionId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa","campaignId":"%s"},
                                  {"id":"22222222-2222-2222-2222-222222222222","impressionId":"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb","campaignId":"%s"}
                                ]
                                """.formatted(CAMPAIGN_ID, CAMPAIGN_ID)));

        verify(dataIngestionService, times(2)).addClick(any());
    }
}
