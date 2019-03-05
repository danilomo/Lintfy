package com.emnify.lint.playground;

import com.emnify.lint.api.ClassFilter;
import com.emnify.lint.api.NodeOperations;
import com.emnify.lint.api.NodeStream;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.javaparsermodel.contexts.CompilationUnitContext;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Danilo Oliveira
 */
public class Main {

    public static TypeSolver SOLVER;

    public static void main(String[] args) throws Exception {

        TypeSolver solver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                JarTypeSolver.getJarTypeSolver("/home/danilo/akka-actor_2.11-2.5.11.jar"),
                JarTypeSolver.getJarTypeSolver("/home/danilo/akka-cluster_2.11-2.5.11.jar"),
                new JavaParserTypeSolver(new File("./src/test/test-sources/kvcluster/kvcluster-common/src/main/java/com/emnify/kvcluster/messages"))
        );

        Main.SOLVER = solver;

        Function<Node, Stream<Node>> getChildren = n
                -> NodeOperations.getChildrenOfClass(
                        n,
                        ClassOrInterfaceDeclaration.class
                );

        Stream<Node> nodes = new NodeStream(
                new File("./src/test/test-sources/main"),
                getChildren
        ).get()
                .flatMap(
                        node -> Stream.concat(
                                Stream.of(node),
                                getChildren.apply(node)
                        )
                );

        ClassFilter<Node, ClassOrInterfaceDeclaration> filter = new ClassFilter<>(
                ClassOrInterfaceDeclaration.class
        );

        Stream<ClassOrInterfaceDeclaration> classes = filter.apply(nodes);

        Map<String, ClassOrInterfaceDeclaration> map = classes
                .collect(Collectors.toMap(
                        n -> n.getName().toString(),
                        n -> n
                ));

        ClassOrInterfaceDeclaration cls = map.get("FrontendActor");

        Context context = new CompilationUnitContext(cls.findCompilationUnit().get(), SOLVER);

        System.out.println(context.solveType("AbstractActor"));

        VoidVisitor<Void> visitor = new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(NameExpr n, Void arg) {
                super.visit(n, arg);

                String rona = "<unsolved>";

                try {
                    rona = n.calculateResolvedType().describe();
                } catch (Exception ex) {
                }

                System.out.println(">>>"
                        + n.getParentNode() + ", "
                        + n
                        + ", "
                        + n.findCompilationUnit().isPresent()
                        + ", "
                        + rona);
            }

        };

        //visitor.visit(cls, null);

    }

}
