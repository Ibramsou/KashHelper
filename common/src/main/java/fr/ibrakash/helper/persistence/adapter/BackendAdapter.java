package fr.ibrakash.helper.persistence.adapter;

/**
 * Shared adapter base that stores the bound repository instance.
 */
public abstract class BackendAdapter<R extends DatabaseRepository> implements DatabaseAdapter<R> {

    protected final R repository;

    protected BackendAdapter(R repository) {
        this.repository = repository;
    }

    /** Exposes the bound repository to operation mixins. */
    public final R repository() {
        return this.repository;
    }

    @Override
    public final void onInit(R repository) {
        // Constructor-based initialization is used by concrete adapters.
    }
}

