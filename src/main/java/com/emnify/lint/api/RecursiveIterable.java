package com.emnify.lint.api;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Function;

/**
 * @param <T>
 * @author Danilo Oliveira
 */
public class RecursiveIterable<T> implements Iterable<T> {

    private final T rootNode;
    private final Function<T, TreeNode<T>> function;

    public RecursiveIterable(T rootNode, Function<T, TreeNode<T>> function) {
        this.rootNode = rootNode;
        this.function = function;
    }

    @Override
    public Iterator<T> iterator() {
        return new RecursiveIterator<>(rootNode, function);
    }

    private static class RecursiveIterator<T> implements Iterator<T> {

        private final Deque<TreeNode<T>> stack;
        private final Function<T, TreeNode<T>> function;

        private RecursiveIterator(T rootNode, Function<T, TreeNode<T>> function) {
            this.function = function;
            this.stack = new ArrayDeque<>();
            this.stack.add(function.apply(rootNode));
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public T next() {
            TreeNode<T> node = stack.poll();

            if (node.hasChildren()) {
                for (T child : node.children()) {
                    stack.add(function.apply(child));
                }
            }

            return node.element();
        }

    }

}
