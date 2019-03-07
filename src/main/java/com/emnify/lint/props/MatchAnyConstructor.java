/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emnify.lint.props;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.types.ResolvedType;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author danilo
 */
public class MatchAnyConstructor implements Predicate<List<Expression>> {
    private final ClassOrInterfaceDeclaration cls;

    public MatchAnyConstructor(ClassOrInterfaceDeclaration cls) {
        this.cls = cls;
    }

    @Override
    public boolean test(List<Expression> list) {
        if (list.isEmpty() && cls.getConstructors().isEmpty()) {
            return true;
        }

        List<ResolvedType> types = list.stream()
            .map(expr -> expr.calculateResolvedType())
            .collect(Collectors.toList());

        for (ConstructorDeclaration constr : cls.getConstructors()) {
            if (testConstructor(constr, types)) {
                return true;
            }
        }

        return false;
    }

    private boolean testConstructor(ConstructorDeclaration constr, List<ResolvedType> list) {
        if (constr.getParameters().size() != list.size()) {
            return false;
        }

        for (int i = 0; i < list.size(); i++) {
            ResolvedType actual = list.get(i);
            ResolvedType formal = constr
                .getParameters()
                .get(i)
                .getType()
                .resolve();

            if (!formal.isAssignableBy(actual)) {
                return false;
            }
        }

        return true;
    }

}
