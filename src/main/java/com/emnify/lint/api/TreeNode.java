package com.emnify.lint.api;

/**
 * @param <T>
 * @author danilo
 */
public interface TreeNode<T> {
    boolean hasChildren();

    Iterable<T> children();

    T element();
}
