package com.garygz.dspdemoproject.service;

import com.garygz.dspdemoproject.controller.dto.CampaignStatsResponse;
import com.garygz.dspdemoproject.controller.exceptions.EntityNotFound;
import com.garygz.dspdemoproject.controller.exceptions.InvalidInputDataProvided;
import com.garygz.dspdemoproject.entity.Advertiser;
import com.garygz.dspdemoproject.entity.Campaign;
import com.garygz.dspdemoproject.repository.AdvertiserRepository;
import com.garygz.dspdemoproject.repository.CampaignRepository;
import com.garygz.dspdemoproject.repository.ClickEventRepository;
import com.garygz.dspdemoproject.repository.ImpressionEventRepository;
import com.garygz.dspdemoproject.service.impl.DashBoardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashBoardServiceImplTest {

    @Mock AdvertiserRepository advRepo;
    @Mock CampaignRepository campaignRepo;
    @Mock ImpressionEventRepository impressionEventRepo;
    @Mock ClickEventRepository clickEventRepo;

    @InjectMocks DashBoardServiceImpl service;

    // ── addAdvertiser ────────────────────────────────────────────────────────

    @Test
    void addAdvertiser_savesWhenValid() {
        Advertiser adv = advertiser("Valid Name");
        when(advRepo.existsAdvertiserByName("Valid Name")).thenReturn(false);

        service.addAdvertiser(adv);

        verify(advRepo).save(adv);
    }

    @Test
    void addAdvertiser_throwsWhenNameIsBlank() {
        assertThatThrownBy(() -> service.addAdvertiser(advertiser("")))
                .isInstanceOf(InvalidInputDataProvided.class)
                .hasMessageContaining("blank");
    }

    @Test
    void addAdvertiser_throwsWhenNameTooShort() {
        assertThatThrownBy(() -> service.addAdvertiser(advertiser("Hi")))
                .isInstanceOf(InvalidInputDataProvided.class)
                .hasMessageContaining("5 chars");
    }

    @Test
    void addAdvertiser_throwsWhenNameAlreadyExists() {
        when(advRepo.existsAdvertiserByName("Acme Corp")).thenReturn(true);

        assertThatThrownBy(() -> service.addAdvertiser(advertiser("Acme Corp")))
                .isInstanceOf(InvalidInputDataProvided.class)
                .hasMessageContaining("already exists");
    }

    // ── addCampaign ──────────────────────────────────────────────────────────

    @Test
    void addCampaign_savesWhenValid() {
        UUID advId = UUID.randomUUID();
        Advertiser adv = advertiser("Acme Corp");
        Campaign campaign = campaign("Summer Sale 2025");

        when(advRepo.findById(advId)).thenReturn(Optional.of(adv));
        when(campaignRepo.existsCampaignByNameAndAdvertiser("Summer Sale 2025", adv)).thenReturn(false);

        service.addCampaign(campaign, advId);

        verify(campaignRepo).save(campaign);
        assertThat(campaign.getAdvertiser()).isEqualTo(adv);
    }

    @Test
    void addCampaign_throwsWhenNameIsBlank() {
        assertThatThrownBy(() -> service.addCampaign(campaign(""), UUID.randomUUID()))
                .isInstanceOf(InvalidInputDataProvided.class)
                .hasMessageContaining("blank");
    }

    @Test
    void addCampaign_throwsWhenNameTooShort() {
        assertThatThrownBy(() -> service.addCampaign(campaign("Ads"), UUID.randomUUID()))
                .isInstanceOf(InvalidInputDataProvided.class)
                .hasMessageContaining("5 chars");
    }

    @Test
    void addCampaign_throwsWhenAdvertiserNotFound() {
        UUID advId = UUID.randomUUID();
        when(advRepo.findById(advId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addCampaign(campaign("Summer Sale"), advId))
                .isInstanceOf(InvalidInputDataProvided.class)
                .hasMessageContaining("Advertiser not found");
    }

    @Test
    void addCampaign_throwsWhenDuplicateName() {
        UUID advId = UUID.randomUUID();
        Advertiser adv = advertiser("Acme Corp");
        Campaign campaign = campaign("Summer Sale");

        when(advRepo.findById(advId)).thenReturn(Optional.of(adv));
        when(campaignRepo.existsCampaignByNameAndAdvertiser("Summer Sale", adv)).thenReturn(true);

        assertThatThrownBy(() -> service.addCampaign(campaign, advId))
                .isInstanceOf(InvalidInputDataProvided.class)
                .hasMessageContaining("already exists");
    }

    // ── getAllAdvertisers ────────────────────────────────────────────────────

    @Test
    void getAllAdvertisers_delegatesToRepo() {
        List<Advertiser> expected = List.of(advertiser("Acme"), advertiser("BetaCo"));
        when(advRepo.findAll()).thenReturn(expected);

        assertThat(service.getAllAdvertisers()).isEqualTo(expected);
    }

    // ── getAllCampaigns ──────────────────────────────────────────────────────

    @Test
    void getAllCampaigns_returnsListForKnownAdvertiser() {
        UUID advId = UUID.randomUUID();
        Advertiser adv = advertiser("Acme Corp");
        List<Campaign> campaigns = List.of(campaign("Summer Sale"), campaign("Winter Push"));

        when(advRepo.findById(advId)).thenReturn(Optional.of(adv));
        when(campaignRepo.findByAdvertiser(adv)).thenReturn(campaigns);

        assertThat(service.getAllCampaigns(advId)).isEqualTo(campaigns);
    }

    @Test
    void getAllCampaigns_throwsEntityNotFoundWhenAdvertiserMissing() {
        UUID advId = UUID.randomUUID();
        when(advRepo.findById(advId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAllCampaigns(advId))
                .isInstanceOf(EntityNotFound.class)
                .hasMessageContaining("Advertiser not found");
    }

    // ── getCampaignStats ────────────────────────────────────────────────────

    @Test
    void getCampaignStats_returnsEmptyListsWhenNoData() {
        UUID campaignId = UUID.randomUUID();
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(impressionEventRepo.countPerDay(campaignId, from, to)).thenReturn(List.of());
        when(clickEventRepo.countPerDay(campaignId, from, to)).thenReturn(List.of());

        CampaignStatsResponse result = service.getCampaignStats(campaignId, from, to);

        assertThat(result.impressionsPerDay()).isEmpty();
        assertThat(result.clicksPerDay()).isEmpty();
    }

    @Test
    void getCampaignStats_mapsRowsCorrectly() {
        UUID campaignId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 7);
        LocalDate day = LocalDate.of(2025, 1, 3);

        List<Object[]> impressionRows = new ArrayList<>();
        impressionRows.add(new Object[]{day, 42L});
        List<Object[]> clickRows = new ArrayList<>();
        clickRows.add(new Object[]{day, 7L});

        when(impressionEventRepo.countPerDay(campaignId, from, to)).thenReturn(impressionRows);
        when(clickEventRepo.countPerDay(campaignId, from, to)).thenReturn(clickRows);

        CampaignStatsResponse result = service.getCampaignStats(campaignId, from, to);

        assertThat(result.impressionsPerDay()).hasSize(1);
        assertThat(result.impressionsPerDay().get(0).date()).isEqualTo(day);
        assertThat(result.impressionsPerDay().get(0).count()).isEqualTo(42L);
        assertThat(result.clicksPerDay()).hasSize(1);
        assertThat(result.clicksPerDay().get(0).date()).isEqualTo(day);
        assertThat(result.clicksPerDay().get(0).count()).isEqualTo(7L);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private static Advertiser advertiser(String name) {
        Advertiser a = new Advertiser();
        a.setName(name);
        return a;
    }

    private static Campaign campaign(String name) {
        Campaign c = new Campaign();
        c.setName(name);
        c.setLandingPage("https://example.com");
        return c;
    }
}
