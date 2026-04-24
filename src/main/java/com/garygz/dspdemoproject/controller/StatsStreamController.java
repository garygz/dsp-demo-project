package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.repository.ClickRepository;
import com.garygz.dspdemoproject.repository.ImpressionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/advertisers/{advertiserId}/campaigns/{campaignId}/stats")
public class StatsStreamController {

    private static final Logger log = LoggerFactory.getLogger(StatsStreamController.class);

    private final ImpressionRepository impressions;
    private final ClickRepository clicks;

    // campaignId → active emitters
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public StatsStreamController(ImpressionRepository impressions, ClickRepository clicks) {
        this.impressions = impressions;
        this.clicks = clicks;
    }

    @GetMapping("/stream")
    public SseEmitter stream(@PathVariable UUID campaignId) {
        log.info("SSE subscriber connected for campaign {}", campaignId);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.computeIfAbsent(campaignId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> {
            List<SseEmitter> list = emitters.get(campaignId);
            if (list != null) list.remove(emitter);
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }

    @Scheduled(fixedRate = 60_000)
    public void pushLatestWindow() {
        if (emitters.isEmpty()) return;

        Instant now         = Instant.now();
        Instant windowStart = now.minus(60, ChronoUnit.SECONDS);
        String occurredAt   = now.truncatedTo(ChronoUnit.MINUTES).toString();
        String fromIso      = windowStart.toString();
        String toIso        = now.toString();

        emitters.forEach((campaignId, list) -> {
            if (list.isEmpty()) return;

            String id = campaignId.toString();
            long imp  = impressions.findByCampaignIdBetween(id, fromIso, toIso).size();
            long clk  = clicks.findByCampaignIdBetween(id, fromIso, toIso).size();
            log.info("campaign={} window=[{} → {}] imp={} clk={}", id, fromIso, toIso, imp, clk);

            Map<String, Object> payload = Map.of(
                "occurredAt",  occurredAt,
                "impressions", imp,
                "clicks",      clk
            );

            List<SseEmitter> dead = new ArrayList<>();
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("tick").data(payload));
                } catch (IOException e) {
                    dead.add(emitter);
                }
            }
            list.removeAll(dead);
        });
    }
}
