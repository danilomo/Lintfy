package com.emnify.lint.api;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @param <T>
 * @param <K>
 * @author danilo
 */
public class ClassFilter<T, K> implements Function<Stream<T>, Stream<K>> {
    private final Class<K> clazz;

    public ClassFilter(Class<K> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Stream<K> apply(Stream<T> stream) {
        return stream
            .filter(element -> element.getClass().isAssignableFrom(clazz))
            .map(element -> (K) element);
    }
}
