package com.garygz.dspdemoproject.repository;

import com.garygz.dspdemoproject.entity.Impression;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.util.List;

@Repository
public class ImpressionRepository {

    private final DynamoDbTable<Impression> table;
    private final DynamoDbEnhancedClient enhancedClient;

    public ImpressionRepository(DynamoDbEnhancedClient enhancedClient,
                                @Value("${DYNAMODB_IMPRESSIONS_TABLE:impressions}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(Impression.class));
        this.enhancedClient = enhancedClient;
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

    // ImpressionRepository.java
    public void saveAll(List<Impression> impressions) {
        // DynamoDB batchWriteItem limit is 25 items per request
        int batchSize = 25;
        for (int i = 0; i < impressions.size(); i += batchSize) {
            List<Impression> chunk = impressions.subList(i, Math.min(i + batchSize, impressions.size()));

            List<WriteBatch> writeBatches = chunk.stream()
                    .map(item -> WriteBatch.builder(Impression.class)
                            .mappedTableResource(table)
                            .addPutItem(item)
                            .build())
                    .toList();

            BatchWriteItemEnhancedRequest request = BatchWriteItemEnhancedRequest.builder()
                    .writeBatches(writeBatches)
                    .build();

            BatchWriteResult result = enhancedClient.batchWriteItem(request);

            /**
             * TODO
             * batchWriteItem can return unprocessed items
             */
            List<Impression> unprocessed = result.unprocessedPutItemsForTable(table);
            if (!unprocessed.isEmpty()) {
                // retry unprocessed with exponential backoff
            }
        }
    }
}
