package com.emnify.lint.api;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Danilo Oliveira
 */
public class JavaPackageSupplier implements Supplier<Stream<File>> {

    private final String rootFolder;
    public static final FilenameFilter FILTER = (dir, name) -> name.endsWith(".java");

    public JavaPackageSupplier(String rootFolder) {
        this.rootFolder = rootFolder;
    }        
    
    @Override
    public Stream<File> get() {
        
        File folder = new File(rootFolder);
        
        Spliterator<File> folders = new FolderIterable(
            folder, f -> f.isDirectory()
        ).spliterator();
        
        return StreamSupport
            .stream(folders, false)
            .filter(f -> f.list(FILTER).length > 0);
    }
    
}
