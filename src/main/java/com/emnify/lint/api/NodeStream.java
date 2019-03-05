package com.emnify.lint.api;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.resolution.SymbolResolver;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Danilo Oliveira
 */
public class NodeStream implements Supplier<Stream<Node>> {

    private final Supplier<Stream<File>> folders;
    private final Function<Node, Stream<Node>> nodeTransformer;
    private Optional<SymbolResolver> symbolResolver = Optional.empty();
    private final JavaParser parser;

    public NodeStream(Supplier<Stream<File>> folders,
            Function<Node, Stream<Node>> nodeTransformer) {
        this.folders = folders;
        this.nodeTransformer = nodeTransformer;
        this.parser = new JavaParser();
    }

    public NodeStream(File rootFolder) {
        this(
            foldersFromRootFolder(rootFolder),
            defaultNodeStreamFunction()
        );
    }
    
    public NodeStream(File rootFolder, 
            Function<Node, Stream<Node>> nodeTransformer) {
        this(
            foldersFromRootFolder(rootFolder),
            nodeTransformer
        );
    }
    
    public NodeStream(Supplier<Stream<File>> folders) {
        this(
            folders,
            defaultNodeStreamFunction()
        );
    }

    private static Supplier<Stream<File>> foldersFromRootFolder(File root) {
        return () -> StreamSupport.stream(
            new FolderIterable(root).spliterator(),
            false
        );
    }

    public NodeStream withSymbolResolver(SymbolResolver symbolResolver) {
        this.symbolResolver = Optional.of(symbolResolver);
        return this;
    }

    private static Function<Node, Stream<Node>> defaultNodeStreamFunction(){
        return node -> Stream.of(node);
    }

    @Override
    public Stream<Node> get() {
        if(symbolResolver.isPresent()){            
            parser.getParserConfiguration().setSymbolResolver(
                symbolResolver.get()
            );
            StaticJavaParser.getConfiguration().setSymbolResolver(symbolResolver.get());
        }
        
        return folders
            .get()
            .flatMap(t -> extractASTIterator(t));
    }

    private Stream<Node> extractASTIterator(File file) {
        try {
            if (file.isDirectory() || !file.getPath().endsWith(".java")) {
                return Stream.of();
            }

            final CompilationUnit cu = parser
                    .parse(file)
                    .getResult()
                    .get();

            Stream<Node> stream = nodeTransformer.apply(cu);

            return stream;
        } catch (FileNotFoundException | RuntimeException ex) {
            return Stream.of();
        }
    }

}
