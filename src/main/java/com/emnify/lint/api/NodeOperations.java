package com.emnify.lint.api;

import com.github.javaparser.ast.Node;
import java.util.stream.Stream;

/**
 *
 * @author danilo
 */
public class NodeOperations {

    public static Stream<Node> getChildrenOfClass(Node node,
            Class<? extends Node> clazz) {
        return node
            .getChildNodes()
            .stream()
            .filter( n -> n != null && n.getClass().isAssignableFrom(clazz) );
    }

}
