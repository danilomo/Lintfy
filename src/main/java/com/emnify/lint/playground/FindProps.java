package com.emnify.lint.playground;

import com.emnify.lint.LintProject;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.javaparsermodel.contexts.CompilationUnitContext;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Danilo Oliveira
 */
public class FindProps {

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
                "/home/danilo/Workspace/AkkaKVStore/kvcluster/kvcluster-client/src/main",
                "/home/danilo/Workspace/AkkaKVStore/kvcluster/kvcluster-common/src/main",
                "/home/danilo/Workspace/AkkaKVStore/kvcluster/kvcluster-core/src/main"
        );

        LintProject project = new LintProject(jars, rootFolders);

        Map<String, Node> map = project
                .compilationUnits()
                .collect(
                        Collectors.toMap(
                                node -> ((CompilationUnit) node).getPackageDeclaration().get().getNameAsString() + "." + ((CompilationUnit) node)
                                .getTypes()
                                .stream()
                                .filter(n -> n.getModifiers().contains(Modifier.publicModifier()))
                                .map(x -> x.getName())
                                .findFirst().get().toString(),
                                node -> node
                        )
                );

        final SymbolReference<ResolvedTypeDeclaration> props = props(project);
        final SymbolReference<ResolvedTypeDeclaration> actorSystem = actorSystem(project);

        VoidVisitor<Void> visitor = new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr expr, Void arg) {
                super.visit(expr, arg);

                if (expr.getNameAsString().equals("create")) {
                    try {
                        Expression scope = expr.getScope().get();
                        ResolvedType type = scope.calculateResolvedType();
                        System.out.println(expr);
                        System.out.println(props);
                        System.out.println(actorSystem);
                        System.out.println(">>>" + type);

                        try {
                            System.out.println(">>> props " + props.getCorrespondingDeclaration().asReferenceType().isAssignableBy(type));
                        } catch (Exception ex) {
                        }

                        try {
                            System.out.println(">>> as " + actorSystem.getCorrespondingDeclaration().asReferenceType().isAssignableBy(type));
                        } catch (Exception ex) {
                        }

                        System.out.println("");

                    } catch (Exception ex) {
//                        System.out.println("pq parouuu " + expr);                        
//                        System.out.println(expr.findCompilationUnit());
                    }
                }
            }
        };

        map.values().forEach(cu -> {
            visitor.visit((CompilationUnit) cu, null);
        });

    }

    private static SymbolReference<ResolvedTypeDeclaration> props(LintProject project) {
        SymbolResolver resolver = project.symbolResolver();

        JavaParser parser = new JavaParser();
        parser.getParserConfiguration().setSymbolResolver(resolver);

        String code = "import akka.actor.Props;";

        Context cont = new CompilationUnitContext(
                parser
                        .parse(code)
                        .getResult()
                        .get(),
                project.typeSolver()
        );

        return cont.solveType("Props");
    }

    private static SymbolReference<ResolvedTypeDeclaration> actorSystem(LintProject project) {
        SymbolResolver resolver = project.symbolResolver();

        JavaParser parser = new JavaParser();
        parser.getParserConfiguration().setSymbolResolver(resolver);

        String code = "import akka.actor.ActorSystem;";

        Context cont = new CompilationUnitContext(
                parser
                        .parse(code)
                        .getResult()
                        .get(),
                project.typeSolver()
        );

        return cont.solveType("ActorSystem");
    }
}
