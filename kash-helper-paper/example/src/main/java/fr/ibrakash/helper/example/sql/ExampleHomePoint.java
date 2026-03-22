package fr.ibrakash.helper.example.sql;

import fr.ibrakash.helper.persistence.entity.PersistedColumn;

public class ExampleHomePoint {

    @PersistedColumn(length = 32)
    private String name;

    @PersistedColumn(length = 64)
    private String world;

    @PersistedColumn(nullable = false)
    private double x;

    @PersistedColumn(nullable = false)
    private double y;

    @PersistedColumn(nullable = false)
    private double z;

    public ExampleHomePoint() {
    }

    public ExampleHomePoint(String name, String world, double x, double y, double z) {
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}

