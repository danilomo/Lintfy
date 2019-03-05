package com.emnify.lint.api;

import com.github.javaparser.ast.Node;
import java.util.Iterator;

/**
 *
 * @author danilo
 */
public class NodeIterable implements Iterable<Node> {

    private final Node node;

    public NodeIterable(Node node) {
        this.node = node;
    }

    @Override
    public Iterator<Node> iterator() {
        return new Node.PostOrderIterator(node);
    }

}
