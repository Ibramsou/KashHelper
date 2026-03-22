package fr.ibrakash.helper.persistence.entity;

/**
 * Controls JSON storage strategy for a persisted entity.
 */
public enum PersistedJsonMode {
    /** Decide automatically from entity annotations (rank -> LOAD_ALL, otherwise LOAD_ON_DEMAND). */
    AUTO,
    /** Keep all entities in one JSON map file and load everything at startup. */
    LOAD_ALL,
    /** Store one JSON file per entity id and load entries on demand. */
    LOAD_ON_DEMAND
}

