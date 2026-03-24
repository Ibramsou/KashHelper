package fr.ibrakash.helper.persistence.adapter;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.mongo.MongoPersistenceEngine;
import org.bson.Document;

/**
 * Common Mongo operation mixin for adapters.
 */
public interface MongoAdapterOperations {

    DatabaseRepository repository();

    default MongoDatabase mongoDatabase() {
        PersistenceEngine engine = repository().engine();
        if (engine instanceof MongoPersistenceEngine mongoEngine) {
            return mongoEngine.getMongoDatabase();
        }
        throw new UnsupportedOperationException("Mongo adapter is not available for backend " + repository().backendType());
    }

    default MongoCollection<Document> collection(String name) {
        return mongoDatabase().getCollection(name);
    }

    default <T> MongoCollection<T> collection(String name, Class<T> documentClass) {
        return mongoDatabase().getCollection(name, documentClass);
    }
}
