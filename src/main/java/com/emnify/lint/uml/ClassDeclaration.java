package com.emnify.lint.uml;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author danilo
 */
public class ClassDeclaration implements UMLElement {

    private final ClassOrInterfaceDeclaration declaration;

    public ClassDeclaration(ClassOrInterfaceDeclaration declaration) {
        this.declaration = declaration;
    }
    
    @Override
    public String get() {        
        Stream<String> fields = declaration
                .getFields()
                .stream()
                .flatMap( field -> field.getVariables().stream() )
                .map( var -> "-" + var.getNameAsString() );
        
        Stream<String> methods = declaration
                .getMethods()
                .stream()
                .map( m -> "+" + m.getTypeAsString() + " " + m.getNameAsString() + "()" );
                
        Stream<Stream<String>> classDeclaration = Stream.of(
            Stream.of("class " + declaration.getNameAsString() + "{"),
            fields,
            Stream.of(""),
            methods,
            Stream.of("}")
        );
        
        return classDeclaration
                .flatMap( f -> f)
                .collect(Collectors.joining("\n"));
    }

    @Override
    public Stream<UMLElement> children() {        
        if(declaration.getExtendedTypes().isEmpty()){
            return Stream.of();
        }
        
        return Stream.of( new ExtensionRelation(declaration) );
    }
  
}
