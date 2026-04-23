package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.repository.ClickEventRepository;
import com.garygz.dspdemoproject.repository.ImpressionEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/advertisers/{advertiserId}/campaigns/{campaignId}/stats")
public class StatsStreamController {

    private final ImpressionEventRepository impressions;
    private final ClickEventRepository clicks;

    // campaignId → active emitters
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public StatsStreamController(ImpressionEventRepository impressions, ClickEventRepository clicks) {
        this.impressions = impressions;
        this.clicks = clicks;
    }

    @GetMapping("/stream")
    public SseEmitter stream(@PathVariable UUID campaignId) {
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

    @Scheduled(cron = "0 * * * * *")
    public void pushLatestMinute() {
        if (emitters.isEmpty()) return;

        Instant now  = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant prev = now.minus(1, ChronoUnit.MINUTES);
        OffsetDateTime from = OffsetDateTime.ofInstant(prev, ZoneOffset.UTC);
        OffsetDateTime to   = OffsetDateTime.ofInstant(now,  ZoneOffset.UTC);

        emitters.forEach((campaignId, list) -> {
            if (list.isEmpty()) return;

            long imp = impressions.sumCountBetween(campaignId, from, to);
            long clk = clicks.sumCountBetween(campaignId, from, to);

            Map<String, Object> payload = Map.of(
                "occurredAt",  prev.toString(),
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
