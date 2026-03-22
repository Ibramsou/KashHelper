package fr.ibrakash.helper.persistence.adapter;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Common Mongo operation mixin for adapters.
 */
public interface MongoAdapterOperations {

    DatabaseRepository repository();

    default MongoDatabase mongoDatabase() {
        return repository().mongoDatabase();
    }

    default MongoCollection<Document> collection(String name) {
        return mongoDatabase().getCollection(name);
    }

    default <T> MongoCollection<T> collection(String name, Class<T> documentClass) {
        return mongoDatabase().getCollection(name, documentClass);
    }
}

