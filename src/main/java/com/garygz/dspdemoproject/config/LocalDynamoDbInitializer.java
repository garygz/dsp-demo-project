package com.garygz.dspdemoproject.config;

import com.garygz.dspdemoproject.repository.ClickRepository;
import com.garygz.dspdemoproject.repository.ImpressionRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

/**
 * Creates DynamoDB tables on startup for local development.
 * In production the tables are provisioned by Terraform — this bean is excluded
 * when the "prod" profile is active.
 */
@Component
@Profile("!prod")
public class LocalDynamoDbInitializer {

    private static final Logger log = LoggerFactory.getLogger(LocalDynamoDbInitializer.class);

    private final ImpressionRepository impressionRepository;
    private final ClickRepository clickRepository;

    public LocalDynamoDbInitializer(ImpressionRepository impressionRepository,
                                    ClickRepository clickRepository) {
        this.impressionRepository = impressionRepository;
        this.clickRepository = clickRepository;
    }

    @PostConstruct
    public void createTablesIfNotExists() {
        createIfMissing(impressionRepository.getTable());
        createIfMissing(clickRepository.getTable());
    }

    private void createIfMissing(software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable<?> table) {
        try {
            table.describeTable();
            log.info("DynamoDB table already exists: {}", table.tableName());
        } catch (ResourceNotFoundException e) {
            table.createTable();
            log.info("Created DynamoDB table: {}", table.tableName());
        }
    }
}
