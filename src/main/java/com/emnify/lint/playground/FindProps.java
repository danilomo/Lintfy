package com.emnify.lint.playground;

import com.emnify.lint.LintProject;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        
        Stream<ClassOrInterfaceDeclaration> classes = project.compilationUnits()
            .map( cu -> (CompilationUnit) cu)
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
        
        VoidVisitor<List<MethodCallExpr>> visitor = new VoidVisitorAdapter<List<MethodCallExpr>>() {            
            private final TypeChecker checker = new TypeChecker(
                Arrays.asList("akka.actor.Props"),
                project.typeSolver()
            );           
            @Override
            public void visit(MethodCallExpr expr, List<MethodCallExpr> arg) {
                super.visit(expr, arg);

                if (expr.getNameAsString().equals("create")) {
                    try {
                        Expression scope = expr.getScope().get();
                        
                        if(checker.test("akka.actor.Props", scope)){
                            arg.add(expr);
                        }
                    } catch (Exception ex) {}
                }
            }
        };

        List<MethodCallExpr> propCreateExprs = new ArrayList<>();
        
        map.values().forEach(cls -> {
            visitor.visit(cls, propCreateExprs);
        });      

        propCreateExprs.forEach( expr -> {
            PropsCreate props = new PropsCreate(expr);
            ResolvedType type = props.actorClass();
            String typeName = type.describe();
            ClassOrInterfaceDeclaration cls = (ClassOrInterfaceDeclaration)
                    map.get(typeName);                        
            
            MatchAnyConstructor predicate = new MatchAnyConstructor(cls);
            
            if(! predicate.test(props.arguments())){
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
                
                System.out.println("");
            }            
        });
    }
    
    private static String getPackageName(Node cu){
        return cu
            .findCompilationUnit()
            .get()
            .getPackageDeclaration()
            .get()
            .getNameAsString();
    }

}
