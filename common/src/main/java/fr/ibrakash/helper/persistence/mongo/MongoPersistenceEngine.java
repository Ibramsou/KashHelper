package fr.ibrakash.helper.persistence.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import fr.ibrakash.helper.configuration.objects.database.ConfigMongo;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.PersistenceType;

public class MongoPersistenceEngine implements PersistenceEngine {

    private final MongoClient client;
    private final MongoDatabase database;

    public MongoPersistenceEngine(ConfigMongo config) {
        this.client = MongoClients.create(config.getUri());
        this.database = this.client.getDatabase(config.getDatabase());
    }

    @Override
    public PersistenceType type() {
        return PersistenceType.MONGODB;
    }

    public MongoDatabase getMongoDatabase() {
        return this.database;
    }

    @Override
    public void close() {
        this.client.close();
    }
}

