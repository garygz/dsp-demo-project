package com.garygz.dspdemoproject.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "impression_events")
public class ImpressionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "occurred_on", nullable = false)
    private LocalDate occurredOn;

    public UUID getId() { return id; }
    public UUID getCampaignId() { return campaignId; }
    public void setCampaignId(UUID campaignId) { this.campaignId = campaignId; }
    public LocalDate getOccurredOn() { return occurredOn; }
    public void setOccurredOn(LocalDate occurredOn) { this.occurredOn = occurredOn; }
}
