package fr.ibrakash.helper.persistence.adapter.mongo;

import fr.ibrakash.helper.persistence.adapter.DatabaseAdapter;
import fr.ibrakash.helper.persistence.adapter.DatabaseRepository;

/**
 * Base class for MongoDB-backed adapters.
 *
 * <p>MongoDB support is optional and addon-provided. This class exposes the
 * {@link com.mongodb.client.MongoDatabase} for raw usage.
 *
 * @param <R> the concrete repository type this adapter belongs to
 */
public abstract class MongoAdapter<R extends DatabaseRepository>
        extends DatabaseAdapter<R>
        implements MongoAdapterOperations {

    protected MongoAdapter(R repository) {
        super(repository);
    }

    /**
     * Returns the live {@link com.mongodb.client.MongoDatabase} from the session engine.
     */
    protected com.mongodb.client.MongoDatabase getDatabase() {
        return mongoDatabase();
    }
}
