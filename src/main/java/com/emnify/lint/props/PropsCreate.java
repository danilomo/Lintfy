/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emnify.lint.props;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

import java.util.Arrays;
import java.util.List;

/**
 * @author danilo
 */
public class PropsCreate {
    private final MethodCallExpr expression;

    public PropsCreate(MethodCallExpr expression) {
        this.expression = expression;
    }

    public ResolvedType actorClass() {
        return expression
            .getArgument(0)
            .asClassExpr()
            .getType()
            .resolve();
    }

    public List<Expression> arguments() {
        if (expression.getArguments().isEmpty()) {
            return Arrays.asList();
        }

        return expression.getArguments().subList(
            1,
            expression.getArguments().size()
        );
    }

}
