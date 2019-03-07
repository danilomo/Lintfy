package com.emnify.lint.fsm;

import com.emnify.lint.LintProject;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Danilo Oliveira
 */
public class FSM {


    public static void main(String[] args) {
        Collection<String> jars = Arrays.asList(
            "/home/danilo/akka-actor_2.11-2.5.11.jar",
            "/home/danilo/akka-cluster_2.11-2.5.11.jar",
            "/home/danilo/scala-library-2.12.8.jar",
            "/home/danilo/Workspace/ESC/"
                + "esc-recovery-test/"
                + "target/esc-recovery-test-2.17.0-SNAPSHOT.jar"
        );

        Collection<String> rootFolders = Arrays.asList(
            "/home/danilo/Workspace/ESC/esc-recovery-test/src/main"
        );

        LintProject project = new LintProject(jars, rootFolders);

        ClassOrInterfaceDeclaration fsm = project
            .publicClasses()
            .get("com.emnify.esc.recoverytest.userequipment.UserEquipmentFSM");

        FSMClassVisitor visitor = new FSMClassVisitor();
        visitor.visit(fsm, null);
    }

    private static class FSMClassVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodCallExpr expr, Void arg) {
            super.visit(expr, arg);

            if (expr.getNameAsString().equals("when")) {
                String state = expr.getArgument(0).toString();
                WhenStatementVisitor wv = new WhenStatementVisitor(state);
                wv.visit(expr, null);
            }
        }
    }

    private static class WhenStatementVisitor extends VoidVisitorAdapter<Void> {
        private final String state;

        public WhenStatementVisitor(String state) {
            this.state = state;
        }

        @Override
        public void visit(MethodCallExpr expr, Void arg) {
            super.visit(expr, null);

            if (expr.getNameAsString().equals("goTo")) {
                System.out.println(state + " -> " + expr.getArgument(0).toString() + ";");
            }
        }
    }
}
