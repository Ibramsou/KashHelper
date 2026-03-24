package fr.ibrakash.helper.binary;

import java.util.Set;

public abstract class BinarySetStorage<E> extends BinaryCollectionStorage<Set<E>, E> {

    protected BinarySetStorage(Set<E> value) {
        super(value);
    }
}

