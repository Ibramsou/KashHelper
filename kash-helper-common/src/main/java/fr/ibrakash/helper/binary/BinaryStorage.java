package fr.ibrakash.helper.binary;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BinaryStorage<T> {

    protected static final byte[] EMPTY_ARRAY = new byte[] {};

    protected T value;
    private byte[] binary;
    private boolean hasUpdated;

    public BinaryStorage(T value) {
        this.value = value;
    }

    public void getUpdateValue(Consumer<T> value) {
        value.accept(this.value);
        this.update();
    }

    public void getUpdateReturnValue(Function<T, T> value) {
        this.value = value.apply(this.value);
        this.update();
    }

    public T getValue() {
        return value;
    }

    public void update() {
        this.hasUpdated = true;
    }

    public void loadValue(byte[] array) {
        this.binary = array;
        this.deserialize(array);
    }

    public byte[] asBinary() {
        if (this.hasUpdated) {
            this.binary = this.serialize(this.value);
            this.hasUpdated = false;
        }

        return this.binary == null ? EMPTY_ARRAY : this.binary;
    }

    protected abstract byte[] serialize(T value);

    protected abstract void deserialize(byte[] array);
}
