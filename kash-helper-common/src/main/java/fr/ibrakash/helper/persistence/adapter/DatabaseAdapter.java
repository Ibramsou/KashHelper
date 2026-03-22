package fr.ibrakash.helper.persistence.adapter;

/**
 * Marker interface for all persistence adapters.
 *
 * <p>Adapters are bound to a specific {@link DatabaseRepository} and are responsible
 * for any backend-specific initialisation that goes beyond what the generic
 * {@link fr.ibrakash.helper.persistence.EntityStore} layer provides.
 *
 * <p>Do not implement this interface directly — extend {@link JsonAdapter} or
 * {@link SqlAdapter} (or {@link MongoAdapter} for MongoDB).
 *
 * @param <R> the concrete repository type this adapter belongs to
 */
public interface DatabaseAdapter<R extends DatabaseRepository> {

    /**
     * Called once when the adapter is registered and the backend session is ready.
     * Use this to pre-load caches, warm up the entity stores, etc.
     */
    void onInit(R repository);
}

