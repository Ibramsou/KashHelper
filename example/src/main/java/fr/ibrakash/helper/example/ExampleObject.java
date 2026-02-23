package fr.ibrakash.helper.example;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ExampleObject {

    private int interval = 20;
    private String localePath = "example-1";

    public ExampleObject() {}

    public ExampleObject(int interval, String localePath) {
        this.interval = interval;
        this.localePath = localePath;
    }

    public int getInterval() {
        return interval;
    }

    public String getLocalePath() {
        return localePath;
    }
}
