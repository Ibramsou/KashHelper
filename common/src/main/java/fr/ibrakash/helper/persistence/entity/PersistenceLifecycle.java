package fr.ibrakash.helper.persistence.entity;

/**
 * Optional lifecycle interface for persisted entities and embedded objects.
 *
 * <p>When an entity (or embedded) implements this interface, its
 * {@link #onDeserialized()} method is called automatically after the
 * persistence system has fully hydrated all fields (columns, blobs,
 * relations, embedded objects).</p>
 *
 * <p>Call order: embedded objects first (bottom-up), then the parent entity.</p>
 *
 * <p>Implementations must be <strong>idempotent</strong>: the hook may be
 * called more than once for the same instance (e.g. after a reload).</p>
 */
public interface PersistenceLifecycle {

    /**
     * Called after the persistence system has fully deserialized this object.
     * Use this to re-inject runtime dependencies, re-register listeners, etc.
     */
    void onDeserialized();
}

