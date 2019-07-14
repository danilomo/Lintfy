package com.emnify.lint;

import com.emnify.lint.api.JavaPackageSupplier;
import com.emnify.lint.api.NodeStream;
import com.emnify.lint.maven.MavenProject;
import com.emnify.lint.maven.MavenTypeSolver;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Danilo Oliveira
 */
public class LintProject {

    private final String rootFolder;
    private final TypeSolver solver;

    public LintProject(String rootFolder, TypeSolver solver) {
        this.rootFolder = rootFolder;
        this.solver = solver;
    }

    public LintProject(MavenProject project) {
        this(
            project.sourceFolder(),
            new MavenTypeSolver(project)
        );
    }

    private static Optional<String> getPackageName(Node cu) {
        return cu
            .findCompilationUnit()
            .flatMap(CompilationUnit::getPackageDeclaration)
            .flatMap(pkg -> Optional.of(pkg.getNameAsString()));
    }

    private Stream<File> javaPackages() {
        return Stream.of(rootFolder)
            .flatMap(f -> new JavaPackageSupplier(f).get());
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
        return new JavaSymbolSolver(solver);
    }

    public TypeSolver typeSolver() {
        return solver;
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
                    cls -> getPackageName(cls)
                        .map(pkg -> pkg + ".")
                        .orElse("") + cls.getName(),
                    cls -> cls
                )
            );
    }

}
