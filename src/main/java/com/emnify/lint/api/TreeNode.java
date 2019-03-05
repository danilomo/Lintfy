package com.emnify.lint.api;

/**
 *
 * @author danilo
 * @param <T>
 */
public interface TreeNode<T> {
    boolean hasChildren();
    Iterable<T> children();
    T element();
}
