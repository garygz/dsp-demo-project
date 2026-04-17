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
import java.util.Optional;

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

    public Optional<Impression> findById(String id, String impressionId) {
        Key key = Key.builder().partitionValue(id).sortValue(impressionId).build();
        return Optional.ofNullable(table.getItem(key));
    }

    public List<Impression> findByImpressionId(String impressionId) {
        QueryConditional query = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(impressionId).build());
        return table.query(query).items().stream().toList();
    }

    public void delete(String id, String impressionId) {
        Key key = Key.builder().partitionValue(id).sortValue(impressionId).build();
        table.deleteItem(key);
    }
}