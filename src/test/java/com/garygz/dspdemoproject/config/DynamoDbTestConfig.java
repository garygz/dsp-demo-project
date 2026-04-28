package com.garygz.dspdemoproject.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Replaces real DynamoDB beans with mocks so integration tests run without
 * any AWS connectivity. Imported explicitly by each @SpringBootTest class.
 *
 * The mock DynamoDbEnhancedClient returns a mock DynamoDbTable from table(),
 * which means:
 *  - ImpressionRepository / ClickRepository construct without NPE
 *  - LocalDynamoDbInitializer.describeTable() returns null (no exception) →
 *    skips createTable() → no real AWS call
 */
@TestConfiguration
public class DynamoDbTestConfig {

    @Bean
    @Primary
    public DynamoDbClient mockDynamoDbClient() {
        return Mockito.mock(DynamoDbClient.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Bean
    @Primary
    public DynamoDbEnhancedClient mockDynamoDbEnhancedClient() {
        DynamoDbEnhancedClient mock = Mockito.mock(DynamoDbEnhancedClient.class);
        DynamoDbTable table = Mockito.mock(DynamoDbTable.class);
        Mockito.when(mock.table(Mockito.anyString(), Mockito.any())).thenReturn(table);
        return mock;
    }
}
