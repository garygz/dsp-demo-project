package com.garygz.dspdemoproject.service.impl;

import com.garygz.dspdemoproject.controller.dto.CampaignStatsResponse;
import com.garygz.dspdemoproject.controller.dto.DailyCount;
import com.garygz.dspdemoproject.controller.exceptions.EntityNotFound;
import com.garygz.dspdemoproject.controller.exceptions.InvalidInputDataProvided;
import com.garygz.dspdemoproject.entity.Advertiser;
import com.garygz.dspdemoproject.entity.Campaign;
import com.garygz.dspdemoproject.repository.AdvertiserRepository;
import com.garygz.dspdemoproject.repository.CampaignRepository;
import com.garygz.dspdemoproject.repository.ClickEventRepository;
import com.garygz.dspdemoproject.repository.ImpressionEventRepository;
import com.garygz.dspdemoproject.service.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DashBoardServiceImpl implements DashBoardService {
    @Autowired
    private AdvertiserRepository advRepo;

    @Autowired
    private CampaignRepository campaignRepo;

    @Autowired
    private ImpressionEventRepository impressionEventRepo;

    @Autowired
    private ClickEventRepository clickEventRepo;

    @Override
    public List<Advertiser> getAllAdvertisers() {
        return advRepo.findAll();
    }

    public List<Advertiser> findAdvertisersByName(String name) {
        return advRepo.findAdvertiserByName(name);
    }

    public void addAdvertiser(Advertiser newAdv){
        if (newAdv.getName() == null || newAdv.getName().isBlank()) throw  new InvalidInputDataProvided("name is blank");
        if (newAdv.getName().length() < 5) throw new InvalidInputDataProvided("name should be more then 5 chars long");
        if (advRepo.existsAdvertiserByName(newAdv.getName())) throw new InvalidInputDataProvided("name already exists");
        advRepo.save(newAdv);
    }

    @Override
    public void addCampaign(Campaign newCampaign, UUID advId) {

        if (newCampaign.getName() == null || newCampaign.getName().isBlank()) throw  new InvalidInputDataProvided("name is blank");
        if (newCampaign.getName().length() < 5) throw new InvalidInputDataProvided("name should be more then 5 chars long");
        Advertiser advertiser = advRepo.findById(advId)
                .orElseThrow(() -> new InvalidInputDataProvided("Advertiser not found"));
        newCampaign.setAdvertiser(advertiser);
        if (campaignRepo.existsCampaignByNameAndAdvertiser(newCampaign.getName(), advertiser)) throw new InvalidInputDataProvided("campaign name already exists");
        campaignRepo.save(newCampaign);
    }

    @Override
    public List<Campaign> getAllCampaigns(UUID advId) {
        Advertiser advertiser = advRepo.findById(advId)
                .orElseThrow(() -> new EntityNotFound("Advertiser not found"));
        return campaignRepo.findByAdvertiser(advertiser);
    }

    @Override
    public CampaignStatsResponse getCampaignStats(UUID campaignId, LocalDate from, LocalDate to) {
        List<DailyCount> impressions = impressionEventRepo.countPerDay(campaignId, from, to)
                .stream()
                .map(row -> new DailyCount((LocalDate) row[0], (long) row[1]))
                .toList();

        List<DailyCount> clicks = clickEventRepo.countPerDay(campaignId, from, to)
                .stream()
                .map(row -> new DailyCount((LocalDate) row[0], (long) row[1]))
                .toList();

        return new CampaignStatsResponse(impressions, clicks);
    }
}
