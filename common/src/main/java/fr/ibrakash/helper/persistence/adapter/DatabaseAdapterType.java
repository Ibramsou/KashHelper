package fr.ibrakash.helper.persistence.adapter;

import fr.ibrakash.helper.persistence.PersistenceType;

/**
 * Maps persistence backend types to their adapter counterpart.
 */
public enum DatabaseAdapterType {
    JSON(PersistenceType.JSON),
    SQL(PersistenceType.SQL),
    MONGO(PersistenceType.MONGODB);

    private final PersistenceType persistenceType;

    DatabaseAdapterType(PersistenceType persistenceType) {
        this.persistenceType = persistenceType;
    }

    public PersistenceType persistenceType() {
        return persistenceType;
    }

    public static DatabaseAdapterType from(PersistenceType type) {
        for (DatabaseAdapterType t : values()) {
            if (t.persistenceType == type) return t;
        }
        throw new IllegalArgumentException("No DatabaseAdapterType for PersistenceType: " + type);
    }
}

