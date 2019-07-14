package com.emnify.lint.maven;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

public class MavenTypeSolver implements TypeSolver {

    private final TypeSolver solver;

    public MavenTypeSolver(MavenProject project) {
        solver = typeSolver(project);
    }

    public TypeSolver typeSolver(MavenProject project) {
        Stream<String> jars = project.jarsFromDependencies();
        Stream<TypeSolver> solvers = typeSolvers(jars);
        TypeSolver[] array = solvers.toArray(TypeSolver[]::new);

        TypeSolver typeSolver = new CombinedTypeSolver(
            new JavaParserTypeSolver(new File(project.sourceFolder())),
            new ReflectionTypeSolver(),
            new CombinedTypeSolver(array)
        );

        return typeSolver;
    }

    private Stream<TypeSolver> typeSolvers(Stream<String> jars) {
        Stream<TypeSolver> jarSolvers = jars
            .flatMap(jar -> solverFromJarPath(jar));
        return jarSolvers;
    }

    private Stream<TypeSolver> solverFromJarPath(String path) {
        try {
            TypeSolver solver = JarTypeSolver.getJarTypeSolver(path);
            return Stream.of(solver);
        } catch (IOException ex) {
            ex.printStackTrace();
            return Stream.of();
        }
    }

    @Override
    public TypeSolver getParent() {
        return solver.getParent();
    }

    @Override
    public void setParent(final TypeSolver typeSolver) {
        solver.setParent(typeSolver);
    }

    @Override
    public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(
        final String type
    ) {
        return solver.tryToSolveType(type);
    }
}
