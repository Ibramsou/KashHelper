package fr.ibrakash.helper.binary;

import java.util.Collection;
import java.util.function.Consumer;

public abstract class BinaryCollectionStorage<C extends Collection<E>, E> extends BinaryStorage<C> {

    protected BinaryCollectionStorage(C value) {
        super(value);
    }

    public void add(E element) {
        this.getUpdateValue(collection -> collection.add(element));
    }

    public void remove(E element) {
        this.getUpdateValue(collection -> collection.remove(element));
    }

    public void clearAll() {
        this.getUpdateValue(Collection::clear);
    }

    public void updateAll(Consumer<C> updater) {
        this.getUpdateValue(updater);
    }
}

