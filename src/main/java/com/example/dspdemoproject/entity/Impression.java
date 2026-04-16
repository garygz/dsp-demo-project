package com.example.dspdemoproject.entity;

import jakarta.persistence.*;

import java.util.UUID;
@Entity
@Table(name = "impressions")
public class Impression {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "campaign_id") // Creates 'campaign_id' column in 'impression' table
    private Campaign campaign;

    private long totalCountFor5sec;

    public long getTotalCountFor5sec() {
        return totalCountFor5sec;
    }

    public void setTotalCountFor5sec(long totalCountFor5sec) {
        this.totalCountFor5sec = totalCountFor5sec;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }
}
