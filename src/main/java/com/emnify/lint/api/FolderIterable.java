package com.emnify.lint.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Danilo Oliveira
 */
public class FolderIterable implements Iterable<File> {

    private final File rootFolder;
    private final Predicate<File> predicate;

    public FolderIterable(File rootFolder) {
        this(rootFolder, f -> true);
    }

    public FolderIterable(File rootFolder, Predicate<File> predicate) {
        this.rootFolder = rootFolder;
        this.predicate = predicate;
    }

    public static void main(String[] args) {
        for (File f : new FolderIterable(new File("src/test/test-sources/"))) {
            System.out.println(f);
        }
    }

    @Override
    public Iterator<File> iterator() {
        return new RecursiveIterable<>(rootFolder, FileNode::new).iterator();
    }

    private class FileNode implements TreeNode<File> {
        private final File file;

        public FileNode(File file) {
            this.file = file;
        }

        @Override
        public boolean hasChildren() {
            return file.isDirectory();
        }

        @Override
        public Iterable<File> children() {
            List<File> files = new ArrayList<>();

            for (File f : file.listFiles()) {
                if (predicate.test(f)) {
                    files.add(f);
                }
            }

            return files;
        }

        @Override
        public File element() {
            return file;
        }
    }

}
