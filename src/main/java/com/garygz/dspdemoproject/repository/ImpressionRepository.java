package com.garygz.dspdemoproject.repository;

import com.garygz.dspdemoproject.entity.Impression;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;

@Repository
public class ImpressionRepository {

    private final DynamoDbTable<Impression> table;

    public ImpressionRepository(DynamoDbEnhancedClient enhancedClient,
                                @Value("${DYNAMODB_IMPRESSIONS_TABLE:impressions}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(Impression.class));
    }

    public DynamoDbTable<Impression> getTable() {
        return table;
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
