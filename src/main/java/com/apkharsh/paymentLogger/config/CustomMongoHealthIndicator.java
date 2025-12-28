package com.apkharsh.paymentLogger.config;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component("mongo")
@RequiredArgsConstructor
public class CustomMongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    @Override
    public Health health() {
        try {
            // Use 'ping' command on YOUR database, not 'local'
            mongoTemplate.getDb().runCommand(new Document("ping", 1));

            return Health.up()
                    .withDetail("database", mongoTemplate.getDb().getName())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}
