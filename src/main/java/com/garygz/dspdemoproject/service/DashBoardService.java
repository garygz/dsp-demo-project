package com.garygz.dspdemoproject.service;

import com.garygz.dspdemoproject.controller.dto.CampaignStatsResponse;
import com.garygz.dspdemoproject.entity.Advertiser;
import com.garygz.dspdemoproject.entity.Campaign;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DashBoardService {

     List<Advertiser> getAllAdvertisers();

     void addAdvertiser(Advertiser newAdv);

     void addCampaign(Campaign newCampaign, UUID advertiserId);

     List<Campaign> getAllCampaigns(UUID advertiserId);

     CampaignStatsResponse getCampaignStats(UUID campaignId, LocalDate from, LocalDate to);
}
