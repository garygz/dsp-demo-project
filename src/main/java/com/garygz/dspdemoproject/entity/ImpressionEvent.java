package com.garygz.dspdemoproject.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@IdClass(ImpressionEventId.class)
@Table(name = "impression_events")
public class ImpressionEvent {

    @Id
    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Id
    @Column(name = "occurred_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime occurredAt;

    @Column(name = "count", nullable = false)
    private long count;

    public UUID getCampaignId() { return campaignId; }
    public void setCampaignId(UUID campaignId) { this.campaignId = campaignId; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
