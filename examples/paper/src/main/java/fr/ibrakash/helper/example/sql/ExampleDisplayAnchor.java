package fr.ibrakash.helper.example.sql;

import fr.ibrakash.helper.persistence.entity.PersistedColumn;

/**
 * Base class used by embedded examples to demonstrate inherited persisted columns.
 */
public class ExampleDisplayAnchor {

    @PersistedColumn(value = "x", nullable = false, defaultValue = "0")
    private int x;

    @PersistedColumn(value = "y", nullable = false, defaultValue = "0")
    private int y;

    @PersistedColumn(value = "z", nullable = false, defaultValue = "0")
    private int z;

    public ExampleDisplayAnchor() {
    }

    public ExampleDisplayAnchor(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}

