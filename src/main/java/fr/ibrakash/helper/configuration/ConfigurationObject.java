package fr.ibrakash.helper.configuration;

public abstract class ConfigurationObject {

    private transient boolean serializableObjectOutdated;

    public boolean isSerializableObjectOutdated() {
        return serializableObjectOutdated;
    }

    public void setSerializableObjectOutdated(boolean serializableObjectOutdated) {
        this.serializableObjectOutdated = serializableObjectOutdated;
    }
}
