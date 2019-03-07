package com.emnify.lint;

import com.emnify.lint.api.JavaPackageSupplier;
import com.emnify.lint.api.NodeStream;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Danilo Oliveira
 */
public class LintProject {

    private final Collection<String> jars;
    private final Collection<String> rootFolders;

    public LintProject(Collection<String> jars, Collection<String> rootFolders) {
        this.jars = jars;
        this.rootFolders = rootFolders;
    }

    private static String getPackageName(Node cu) {
        return cu
            .findCompilationUnit()
            .get()
            .getPackageDeclaration()
            .get()
            .getNameAsString();
    }

    private Stream<File> javaPackages() {
        return rootFolders.stream()
            .flatMap(f -> new JavaPackageSupplier(f).get());
    }

    private Stream<TypeSolver> typeSolvers() {
        Stream<TypeSolver> jarSolvers = jars
            .stream()
            .flatMap(jar -> solverFromJarPath(jar));

        return jarSolvers;
    }

    private Stream<TypeSolver> solverFromJarPath(String path) {
        try {
            TypeSolver solver = JarTypeSolver.getJarTypeSolver(path);
            return Stream.of(solver);
        } catch (IOException ex) {
            return Stream.of();
        }
    }

    public Stream<Node> compilationUnits() {
        FilenameFilter filter = JavaPackageSupplier.FILTER;

        Supplier<Stream<File>> sourceFiles = () -> javaPackages()
            .flatMap(
                folder -> Arrays.asList(folder.list(filter))
                    .stream()
                    .map(str -> new File(folder.getAbsolutePath() + "/" + str))
            );

        SymbolResolver resolver = symbolResolver();

        return new NodeStream(sourceFiles).withSymbolResolver(
            resolver
        ).get();
    }

    public SymbolResolver symbolResolver() {
        TypeSolver typeSolver = typeSolver();
        return new JavaSymbolSolver(typeSolver);
    }

    public TypeSolver typeSolver() {
        Stream<TypeSolver> solvers = typeSolvers();
        TypeSolver[] array = solvers.toArray(TypeSolver[]::new);
        TypeSolver typeSolver = new CombinedTypeSolver(
            new ReflectionTypeSolver(),
            new CombinedTypeSolver(array)
        );
        return typeSolver;
    }

    public Map<String, ClassOrInterfaceDeclaration> publicClasses() {
        Stream<ClassOrInterfaceDeclaration> classes = compilationUnits()
            .map(cu -> (CompilationUnit) cu)
            .flatMap(cu -> cu.getTypes().stream())
            .filter(cls -> cls instanceof ClassOrInterfaceDeclaration)
            .filter(cls -> cls.getModifiers().contains(Modifier.publicModifier()))
            .map(cls -> (ClassOrInterfaceDeclaration) cls);

        return classes
            .collect(
                Collectors.toMap(
                    cls -> getPackageName(cls) + "." + cls.getName(),
                    cls -> cls
                )
            );
    }

}
