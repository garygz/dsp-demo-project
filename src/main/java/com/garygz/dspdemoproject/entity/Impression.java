package com.garygz.dspdemoproject.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class Impression {

    private String campaignId; // partition key — co-locates all impressions for a campaign
    private String timestamp;  // sort key — ISO-8601, lexicographic order == time order
    private String id;         // original client-generated UUID, stored as attribute

    public Impression() {}

    @DynamoDbPartitionKey
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    @DynamoDbSortKey
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
