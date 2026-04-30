package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.entity.Click;
import com.garygz.dspdemoproject.entity.Impression;
import com.garygz.dspdemoproject.service.DataIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
public class DataIngestionController {

    private static final Logger logger = LoggerFactory.getLogger(DataIngestionController.class);

    private final DataIngestionService dataIngestionService;

    public DataIngestionController(DataIngestionService dataIngestionService) {
        this.dataIngestionService = dataIngestionService;
    }

    public record ImpressionRequest(UUID id, UUID campaignId) {}
    public record ClickRequest(UUID id, UUID impressionId, UUID campaignId) {}

    @PostMapping("/impressions")
    public ResponseEntity<Void> addImpression(@RequestBody ImpressionRequest body) {
        Impression impression = new Impression();
        impression.setId(body.id().toString());
        impression.setCampaignId(body.campaignId().toString());
        impression.setTimestamp(Instant.now().toString());
        logger.debug("Recording impression {} for campaign {}", body.id(), body.campaignId());
        dataIngestionService.addImpression(impression);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/clicks")
    public ResponseEntity<Void> addClick(@RequestBody ClickRequest body) {
        Click click = new Click();
        click.setId(body.id().toString());
        click.setImpressionId(body.impressionId().toString());
        click.setCampaignId(body.campaignId().toString());
        click.setTimestamp(Instant.now().toString());
        logger.debug("Recording click {} for impression {} campaign {}", body.id(), body.impressionId(), body.campaignId());
        dataIngestionService.addClick(click);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/impressions/batch")
    public ResponseEntity<Void> addImpressions(@RequestBody List<ImpressionRequest> body) {
        logger.debug("Recording batch of {} impressions", body.size());
        List<Impression> impressions = body.stream().map(req -> {
            Impression impression = new Impression();
            impression.setId(req.id().toString());
            impression.setCampaignId(req.campaignId().toString());
            impression.setTimestamp(Instant.now().toString());
            return impression;
        }).toList();
        dataIngestionService.addImpressions(impressions);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/clicks/batch")
    public ResponseEntity<Void> addClicks(@RequestBody List<ClickRequest> body) {
        logger.debug("Recording batch of {} clicks", body.size());
        body.forEach(req -> {
            Click click = new Click();
            click.setId(req.id().toString());
            click.setImpressionId(req.impressionId().toString());
            click.setCampaignId(req.campaignId().toString());
            click.setTimestamp(Instant.now().toString());
            dataIngestionService.addClick(click);
        });
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
