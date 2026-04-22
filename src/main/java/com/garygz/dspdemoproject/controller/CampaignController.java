package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.controller.dto.CampaignStatsResponse;
import com.garygz.dspdemoproject.entity.Campaign;
import com.garygz.dspdemoproject.service.DashBoardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/advertisers/{advertiserId}/campaigns")
public class CampaignController {

    private static final Logger logger = LoggerFactory.getLogger(CampaignController.class);

    private final DashBoardService dashBoardService;

    public CampaignController(DashBoardService dashBoardService) {
        this.dashBoardService = dashBoardService;
    }

    @GetMapping
    public ResponseEntity<List<Campaign>> listAll(@PathVariable UUID advertiserId) {
        return ResponseEntity.ok(dashBoardService.getAllCampaigns(advertiserId));
    }

    @PostMapping
    public ResponseEntity<Void> add(@RequestBody Campaign newCampaign, @PathVariable UUID advertiserId) {
        logger.debug("Creating campaign {}", newCampaign.getName());
        dashBoardService.addCampaign(newCampaign, advertiserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{campaignId}/stats")
    public ResponseEntity<CampaignStatsResponse> getStats(
            @PathVariable UUID advertiserId,
            @PathVariable UUID campaignId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(dashBoardService.getCampaignStats(campaignId, from, to));
    }
}
