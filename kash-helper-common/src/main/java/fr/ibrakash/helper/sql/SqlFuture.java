package fr.ibrakash.helper.sql;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SqlFuture<V> {

    public static <V> SqlFuture<V> of(CompletableFuture<V> future) {
        return new SqlFuture<>(future);
    }

    private final CompletableFuture<V> future;

    SqlFuture(CompletableFuture<V> future) {
        this.future = future;
    }

    public void getOrOptional(Consumer<Optional<V>> found) {
        this.getOrNull(v -> found.accept(Optional.ofNullable(v)));
    }

    public void getOrNull(Consumer<V> found) {
        this.getIfPresent(found, () -> found.accept(null));
    }

    public void getIfPresentOnly(Consumer<V> found) {
        this.getIfPresent(found, null);
    }

    public void getIfPresent(Consumer<V> found, @Nullable Runnable notFound) {
        this.future.thenAccept(result -> {
            if (result == null) {
                if (notFound != null) notFound.run();
                return;
            }

            found.accept(result);
        });
    }
}
