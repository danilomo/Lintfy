/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emnify.lint.api;

import com.github.javaparser.ast.Node;
import java.util.Iterator;

/**
 *
 * @author Danilo Oliveira
 * @param <T>
 */
public class ChildrenIterable<T> implements Iterable<T> {
    private final Node node;
    private final Class<T> clazz;

    public ChildrenIterable(Node node, Class<T> clazz) {
        this.node = node;
        this.clazz = clazz;
    }

    @Override
    public Iterator<T> iterator() {
        return node.getChildNodes()
            .stream()
            .filter( n -> n.getClass().isAssignableFrom(clazz) )
            .map( n -> (T) n ).iterator();
    }
        
}
