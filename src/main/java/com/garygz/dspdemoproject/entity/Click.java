package com.garygz.dspdemoproject.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class Click {

    private String campaignId;   // partition key
    private String timestamp;    // sort key — ISO-8601
    private String id;           // client-generated UUID
    private String impressionId; // reference back to the originating impression

    public Click() {}

    @DynamoDbPartitionKey
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    @DynamoDbSortKey
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImpressionId() { return impressionId; }
    public void setImpressionId(String impressionId) { this.impressionId = impressionId; }
}
