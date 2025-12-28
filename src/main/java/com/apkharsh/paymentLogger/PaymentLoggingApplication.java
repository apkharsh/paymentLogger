package com.apkharsh.paymentLogger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.mongodb.autoconfigure.health.MongoHealthContributorAutoConfiguration;

@SpringBootApplication(exclude = {MongoHealthContributorAutoConfiguration.class})
public class PaymentLoggingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentLoggingApplication.class, args);
    }

}