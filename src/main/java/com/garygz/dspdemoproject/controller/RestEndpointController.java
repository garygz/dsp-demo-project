package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.entity.Advertiser;
import com.garygz.dspdemoproject.entity.Campaign;
import com.garygz.dspdemoproject.entity.Click;
import com.garygz.dspdemoproject.entity.Impression;
import com.garygz.dspdemoproject.service.DashBoardService;
import com.garygz.dspdemoproject.service.DataIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class RestEndpointController {
    private static final Logger logger = LoggerFactory.getLogger(RestEndpointController.class);

    @Autowired
    private DashBoardService dashBoardService;

    @Autowired
    private DataIngestionService dataIngestionService;

    @GetMapping(value = "/advertisers")
    public ResponseEntity<List<Advertiser>> listAllAdvertisers() {
        return ResponseEntity.ok(dashBoardService.getAllAdvertisers());
    }

    @PostMapping(value = "/advertisers")
    public ResponseEntity<Void> addAdvertiser(@RequestBody Advertiser newAdv) {
        logger.debug("Creating advertiser {}", newAdv.getName());
        dashBoardService.addAdvertiser(newAdv);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("/advertisers/{advertiserId}/campaigns")
    public ResponseEntity<List<Campaign>> listAllCampaigns(@PathVariable UUID advertiserId) {
        return ResponseEntity.ok(dashBoardService.getAllCampaigns(advertiserId));
    }

    @PostMapping(value = "/advertisers/{advertiserId}/campaigns")
    public ResponseEntity<Void> addCampaign(@RequestBody Campaign newCampaign, @PathVariable UUID advertiserId) {
        logger.debug("Creating newCampaign {}", newCampaign.getName());
        dashBoardService.addCampaign(newCampaign, advertiserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(value = "/advertisers/{advertiserId}/campaigns/{campaignId}/impressions")
    public ResponseEntity<Void> addImpression(@RequestBody Impression impression, @PathVariable UUID advertiserId,  @PathVariable UUID campaignId) {
        logger.debug("Creating impression {}", impression.getCampaignId());
        dataIngestionService.addImporession(impression);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(value = "/advertisers/{advertiserId}/campaigns/{campaignId}/impressions/{impressionId}/clicks")
    public ResponseEntity<Void> addClick(@RequestBody Click click, @PathVariable UUID advertiserId, @PathVariable UUID campaignId, @PathVariable UUID impressionId) {
        logger.debug("Creating click {}, impression {}", click.getId(), click.getImpressionId());
        dataIngestionService.addClick(click);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }



}
