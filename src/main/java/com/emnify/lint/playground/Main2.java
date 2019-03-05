package com.emnify.lint.playground;


import com.emnify.lint.LintProject;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;


/**
 *
 * @author danilo
 */
public class Main2 {
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
                                .map( x -> x.getName() )
                                .findFirst().get().toString(),
                        node -> node
                    )
                );

        VoidVisitor<Void> visitor = new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr expr, Void arg) {
               super.visit(expr, arg);
               
               if(expr.toString().contains("actorOf")){
                   System.out.println(">>>" + expr);
                   System.out.println(">>>" + expr.getName());
                   System.out.println(">>>" + expr.getArguments());
                   System.out.println(">>>" + expr.getParentNode());
                   try{System.out.println(">>>" + expr.getScope().get().calculateResolvedType());} catch(Exception e){}
                   System.out.println("");
               }
            }
        };
        
        map.values().forEach(cu ->{
            visitor.visit((CompilationUnit) cu,null);
        });
        
    }
}
