/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emnify.lint.akka;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.javaparsermodel.contexts.CompilationUnitContext;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * @author danilo
 */
public class TypeChecker implements BiPredicate<String, Expression> {
    private final List<String> types;
    private final TypeSolver solver;
    private final Context context;

    public TypeChecker(List<String> types, TypeSolver solver) {
        this.types = types;
        this.solver = solver;
        this.context = initContext();
    }

    private Context initContext() {
        JavaParser parser = new JavaParser();
        parser.getParserConfiguration()
            .setSymbolResolver(new JavaSymbolSolver(solver));

        String code = types.stream()
            .map(type -> "import " + type + ";")
            .collect(Collectors.joining("\n"));

        return new CompilationUnitContext(
            parser
                .parse(code)
                .getResult()
                .get(),
            solver
        );
    }

    @Override
    public boolean test(String type, Expression expr) {
        try {
            ResolvedType exprType = expr.calculateResolvedType();
            ResolvedReferenceTypeDeclaration checkingType = context
                .solveType(type)
                .getCorrespondingDeclaration()
                .asReferenceType();

            return checkingType.isAssignableBy(exprType);
        } catch (Exception ex) {
            return false;
        }
    }

}
