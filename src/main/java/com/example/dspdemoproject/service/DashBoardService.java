package com.example.dspdemoproject.service;

import com.example.dspdemoproject.entity.Advertiser;
import com.example.dspdemoproject.entity.Campaign;

import java.util.List;
import java.util.UUID;

public interface DashBoardService {

    public List<Advertiser> getAllAdvertisers();

    public void addAdvertiser(Advertiser newAdv);

    public void addCampaign(Campaign newCampaign, UUID advertiserId);

    public List<Campaign>getAllCampaigns(UUID advertiserId);
}
