package com.apkharsh.paymentLogger.config;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component("customMongoHealth")
@ConditionalOnProperty(prefix = "management.health.mongo", name = "enabled", havingValue = "false")
@RequiredArgsConstructor
public class CustomMongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    @Override
    public Health health() {
        try {
            Document result = mongoTemplate.getDb().runCommand(new Document("ping", 1));
            return Health.up()
                    .withDetail("database", mongoTemplate.getDb().getName())
                    .withDetail("status", "Connected")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
