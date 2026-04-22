package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.entity.Click;
import com.garygz.dspdemoproject.entity.Impression;
import com.garygz.dspdemoproject.service.DataIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class DataIngestionController {

    private static final Logger logger = LoggerFactory.getLogger(DataIngestionController.class);

    private final DataIngestionService dataIngestionService;

    public DataIngestionController(DataIngestionService dataIngestionService) {
        this.dataIngestionService = dataIngestionService;
    }

    public record ImpressionRequest(UUID id, UUID campaignId) {}
    public record ClickRequest(UUID id, UUID impressionId) {}

    @PostMapping("/impressions")
    public ResponseEntity<Void> addImpression(@RequestBody ImpressionRequest body) {
        Impression impression = new Impression();
        impression.setId(body.id());
        impression.setCampaignId(body.campaignId());
        logger.debug("Recording impression {} for campaign {}", body.id(), body.campaignId());
        dataIngestionService.addImpression(impression);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/clicks")
    public ResponseEntity<Void> addClick(@RequestBody ClickRequest body) {
        Click click = new Click();
        click.setId(body.id());
        click.setImpressionId(body.impressionId());
        logger.debug("Recording click {} for impression {}", body.id(), body.impressionId());
        dataIngestionService.addClick(click);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
