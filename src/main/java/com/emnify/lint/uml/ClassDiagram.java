package com.emnify.lint.uml;

import com.emnify.lint.LintProject;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Danilo Oliveira
 */
public class ClassDiagram implements Supplier<String> {

    private final LintProject project;

    public ClassDiagram(LintProject project) {
        this.project = project;
    }

    public static void main(String[] args) {
        Collection<String> jars = Arrays.asList(
            "/home/danilo/akka-actor_2.11-2.5.11.jar",
            "/home/danilo/akka-cluster_2.11-2.5.11.jar",
            "/home/danilo/scala-library-2.12.8.jar",
            "/home/danilo/Workspace/AkkaKVStore/kvcluster/kvcluster-client/target/kvcluster-client-0.0.1.jar",
            "/home/danilo/Workspace/AkkaKVStore/kvcluster/kvcluster-common/target/kvcluster-common-0.0.1.jar",
            "/home/danilo/Workspace/AkkaKVStore/kvcluster/kvcluster-core/target/kvcluster-core-0.0.1.jar"
        );

        Collection<String> rootFolders = Arrays.asList(
            //"/home/danilo/Workspace/AkkaKVStore/kvcluster/kvcluster-client/src/main" ,
            "/home/danilo/Workspace/AkkaKVStore/kvcluster/kvcluster-common/src/main" //,
            //"/home/danilo/Workspace/AkkaKVStore/kvcluster/kvcluster-core/src/main"
        );
        LintProject project = new LintProject(null);

        Stream<CompilationUnit> cus = project
            .compilationUnits()
            .map(n -> (CompilationUnit) n);

        Stream<ClassOrInterfaceDeclaration> classes = cus
            .flatMap(cu -> cu.getTypes().stream())
            .filter(cls -> cls instanceof ClassOrInterfaceDeclaration)
            .filter(cls -> cls.getModifiers().contains(Modifier.publicModifier()))
            .map(cls -> (ClassOrInterfaceDeclaration) cls);


        Stream<String> stream = classes
            .map(ClassDeclaration::new)
            .flatMap(cls -> Stream.concat(Stream.of(cls), cls.children()))
            .map(element -> element.get());

        System.out.println(
            Stream.concat(
                Stream.of("@startuml"),
                Stream.concat(stream, Stream.of("@enduml"))
            ).collect(Collectors.joining("\n\n"))
        );
    }

    @Override
    public String get() {
        return "";
    }

    private static class NameFromType implements Function<ClassOrInterfaceDeclaration, String> {
        @Override
        public String apply(ClassOrInterfaceDeclaration type) {
            Optional<String> pkgName = type
                .findCompilationUnit()
                .flatMap(cu -> cu.getPackageDeclaration())
                .map(pkg -> pkg.getNameAsString());

            return pkgName.orElse("default") + "." + type.getNameAsString();
        }
    }


}
