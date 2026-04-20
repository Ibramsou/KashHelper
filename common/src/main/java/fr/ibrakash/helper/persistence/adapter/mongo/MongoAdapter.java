package fr.ibrakash.helper.persistence.adapter.mongo;

import fr.ibrakash.helper.persistence.adapter.DatabaseAdapter;
import fr.ibrakash.helper.persistence.adapter.DatabaseRepository;

public abstract class MongoAdapter<R extends DatabaseRepository>
        extends DatabaseAdapter<R>
        implements MongoAdapterOperations {

    protected MongoAdapter(R repository) {
        super(repository);
    }

    
    protected com.mongodb.client.MongoDatabase getDatabase() {
        return mongoDatabase();
    }
}
