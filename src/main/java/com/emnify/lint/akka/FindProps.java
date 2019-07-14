package com.emnify.lint.akka;

import com.emnify.lint.LintProject;
import com.emnify.lint.maven.MavenProject;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Danilo Oliveira
 */
public class FindProps {

    private static class PropsVisitor extends VoidVisitorAdapter<List<MethodCallExpr>> {
        private final TypeChecker checker;
        public PropsVisitor(LintProject project) {
            this.checker = new TypeChecker(
                Arrays.asList("akka.actor.Props"),
                project.typeSolver()
            );
        }

        @Override
        public void visit(MethodCallExpr expr, List<MethodCallExpr> arg) {
            super.visit(expr, arg);
            if (expr.getNameAsString().equals("create")) {
                try {
                    Expression scope = expr.getScope().get();

                    if (checker.test("akka.actor.Props", scope)) {
                        arg.add(expr);
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    public static void main(String[] args) {
        MavenProject prj = new MavenProject("/home/danilo/Workspace/" +
            "github/AkkaKVStore/kvcluster/" +
            "kvcluster-core/pom.xml");

        prj.jarsFromDependencies().forEach( s -> {
            System.out.println(">>>" + s);
        });

        LintProject project = new LintProject(prj);

        Stream<ClassOrInterfaceDeclaration> classes = project.compilationUnits()
            .map(cu -> (CompilationUnit) cu)
            .flatMap(cu -> cu.getTypes().stream())
            .filter(cls -> cls instanceof ClassOrInterfaceDeclaration)
            .filter(cls -> cls.getModifiers().contains(Modifier.publicModifier()))
            .map(cls -> (ClassOrInterfaceDeclaration) cls);

        Map<String, ClassOrInterfaceDeclaration> map = classes
            .collect(
                Collectors.toMap(
                    cls -> getPackageName(cls) + "." + cls.getName(),
                    cls -> cls
                )
            );

        List<MethodCallExpr> propCreateExprs = new ArrayList<>();
        PropsVisitor visitor = new PropsVisitor(project);

        map.values().forEach(cls -> {
            visitor.visit(cls, propCreateExprs);
        });

        propCreateExprs.forEach(expr -> {
            PropsCreate props = new PropsCreate(expr);
            System.out.println(">>> " + props.arguments());
            ResolvedType type = props.actorClass();
            String typeName = type.describe();
            ClassOrInterfaceDeclaration cls = map.get(typeName);

            MatchAnyConstructor predicate = new MatchAnyConstructor(cls);

            if (!predicate.test(props.arguments())) {
                System.out.println("Props that doesn't match a constructor: ");
                System.out.println(expr);
                System.out.println("At: "
                    + getPackageName(expr.findCompilationUnit().get())
                    + "."
                    + expr
                    .findCompilationUnit()
                    .get()
                    .getPrimaryType()
                    .get()
                    .getNameAsExpression()
                    + ", line "

                    + expr.getRange().get().begin.line);

                System.out.println();
            }
        });
    }

    private static String getPackageName(Node cu) {
        return cu
            .findCompilationUnit()
            .get()
            .getPackageDeclaration()
            .get()
            .getNameAsString();
    }

}
