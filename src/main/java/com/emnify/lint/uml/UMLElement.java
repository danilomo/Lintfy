package com.emnify.lint.uml;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Danilo Oliveira
 */
public interface UMLElement extends Supplier<String> {
    default Stream<UMLElement> children() {
        return Stream.of();
    }
}
