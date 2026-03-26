package fr.ibrakash.helper.persistence.adapter;

/**
 * Shared adapter base that stores the bound repository instance.
 */
public abstract class DatabaseAdapter<R extends DatabaseRepository> {

    protected final R repository;

    protected DatabaseAdapter(R repository) {
        this.repository = repository;
    }

    /** Exposes the bound repository to operation mixins. */
    public final R repository() {
        return this.repository;
    }
}

