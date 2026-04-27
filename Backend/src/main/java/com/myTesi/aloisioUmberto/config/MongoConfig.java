package com.myTesi.aloisioUmberto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.myTesi.aloisioUmberto.data.dao")

public class MongoConfig {
}