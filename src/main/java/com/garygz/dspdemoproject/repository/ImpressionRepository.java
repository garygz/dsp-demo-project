package com.garygz.dspdemoproject.repository;

import com.garygz.dspdemoproject.entity.Impression;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.List;

@Repository
public class ImpressionRepository {

    private static final String TABLE_NAME = "impressions";

    private final DynamoDbTable<Impression> table;

    public ImpressionRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(Impression.class));
    }

    @PostConstruct
    public void createTableIfNotExists() {
        try {
            table.describeTable();
        } catch (ResourceNotFoundException e) {
            table.createTable();
        }
    }

    public void save(Impression impression) {
        table.putItem(impression);
    }

    public List<Impression> findByCampaignIdBetween(String campaignId, String fromTimestamp, String toTimestamp) {
        QueryConditional query = QueryConditional.sortBetween(
                Key.builder().partitionValue(campaignId).sortValue(fromTimestamp).build(),
                Key.builder().partitionValue(campaignId).sortValue(toTimestamp).build()
        );
        return table.query(query).items().stream().toList();
    }
}
