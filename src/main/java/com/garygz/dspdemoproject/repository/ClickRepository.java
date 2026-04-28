package com.garygz.dspdemoproject.repository;

import com.garygz.dspdemoproject.entity.Click;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;

@Repository
public class ClickRepository {

    private final DynamoDbTable<Click> table;

    public ClickRepository(DynamoDbEnhancedClient enhancedClient,
                           @Value("${DYNAMODB_CLICKS_TABLE:clicks}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(Click.class));
    }

    public DynamoDbTable<Click> getTable() {
        return table;
    }

    public void save(Click click) {
        table.putItem(click);
    }

    public List<Click> findByCampaignIdBetween(String campaignId, String fromTimestamp, String toTimestamp) {
        QueryConditional query = QueryConditional.sortBetween(
                Key.builder().partitionValue(campaignId).sortValue(fromTimestamp).build(),
                Key.builder().partitionValue(campaignId).sortValue(toTimestamp).build()
        );
        return table.query(query).items().stream().toList();
    }
}
