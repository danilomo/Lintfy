package com.emnify.lint.uml;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 *
 * @author danilo
 */
public class ExtensionRelation implements UMLElement{

    private final ClassOrInterfaceDeclaration declaration;

    public ExtensionRelation(ClassOrInterfaceDeclaration declaration) {
        this.declaration = declaration;
    }        
    
    @Override
    public String get() {
        return declaration.getExtendedTypes().get(0).getNameAsString()
                + " <|-- " 
                + declaration.getNameAsString();
    }
   
}
