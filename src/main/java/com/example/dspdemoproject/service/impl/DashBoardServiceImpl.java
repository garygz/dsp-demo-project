package com.example.dspdemoproject.service.impl;

import com.example.dspdemoproject.controller.exceptions.InvalidInputDataProvided;
import com.example.dspdemoproject.entity.Advertiser;
import com.example.dspdemoproject.entity.Campaign;
import com.example.dspdemoproject.repository.AdvertiserRepository;
import com.example.dspdemoproject.repository.CampaignRepository;
import com.example.dspdemoproject.service.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class DashBoardServiceImpl implements DashBoardService {
    @Autowired
    private AdvertiserRepository advRepo;

    @Autowired
    private CampaignRepository campaignRepo;

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
                .orElseThrow(() -> new RuntimeException("Advertiser not found"));
        return campaignRepo.findByAdvertiser(advertiser);
    }

}
