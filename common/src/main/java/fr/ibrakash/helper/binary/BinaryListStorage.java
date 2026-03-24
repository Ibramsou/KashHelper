package fr.ibrakash.helper.binary;

import java.util.List;

public abstract class BinaryListStorage<E> extends BinaryCollectionStorage<List<E>, E> {

    protected BinaryListStorage(List<E> value) {
        super(value);
    }
}

