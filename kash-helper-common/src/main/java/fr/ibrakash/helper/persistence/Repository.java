package fr.ibrakash.helper.persistence;

/**
 * Base contract for all repository implementations.
 *
 * <p>Each concrete repository is responsible for its own statements / serialisation.
 * The framework only injects the backend resources via {@link PersistenceEngine}.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@link #init(PersistenceEngine)}  — called once after construction (create table, etc.)</li>
 *   <li>{@link #loadAll()}                — populate the in-memory cache at startup</li>
 *   <li>{@link #saveAll()}                — flush the cache to the backend (shutdown / periodic)</li>
 * </ol>
 */
public interface Repository {

    /**
     * Receives the live {@link PersistenceEngine} and performs one-time
     * initialisation (e.g. {@code CREATE TABLE IF NOT EXISTS}).
     */
    void init(PersistenceEngine engine);

    /** Loads all persisted data into the in-memory cache. */
    void loadAll();

    /** Flushes the full in-memory cache back to the backend. */
    void saveAll();
}

