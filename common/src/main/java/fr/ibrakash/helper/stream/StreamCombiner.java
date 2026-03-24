package fr.ibrakash.helper.stream;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StreamCombiner<V> {

    private Stream<V> stream;

    private final List<Comparator<V>> sorters = new ArrayList<>();

    public StreamCombiner(Stream<V> stream) {
        this.stream = stream;
    }

    public StreamCombiner<V> filter(Predicate<V> filter) {
        this.stream = this.stream.filter(filter);
        return this;
    }

    public StreamCombiner<V> sort(Comparator<V> sorter) {
        this.sorters.add(sorter);
        return this;
    }

    public Stream<V> build() {
        Iterator<Comparator<V>> iterator = this.sorters.iterator();
        Comparator<V> comparator = null;
        while (iterator.hasNext()) {
            if (comparator != null) {
                comparator = comparator.thenComparing(iterator.next());
            } else {
                comparator = iterator.next();
            }
        }

        if (comparator != null) this.stream = this.stream.sorted(comparator);
        return this.stream;

    }

}
