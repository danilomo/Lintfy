package com.emnify.lint.maven;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public class MavenProject {
    private final File file;
    private final Model pomModel;

    public MavenProject(File file) {
        this.file = file;
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            pomModel = reader.read(new FileInputStream(file));
        } catch (Exception exception) {
            throw new MavenException("Invalid pom file.");
        }
    }

    public MavenProject(String path) {
        this(new File(path));
    }

    public Stream<String> jarsFromDependencies() {
        final MavenArtifact artifact = MavenArtifact.fromModel(pomModel);
        Stream<File> jarFiles = Stream.concat(
            artifact.dependencies().stream(),
            artifact.transientDependencies().stream()
        ).map(str -> new File(str.jarPath()));
        return jarFiles
            .filter(file -> file.exists() && !file.isDirectory())
            .map(File::getAbsolutePath);
    }

    public String sourceFolder() {
        return Optional.ofNullable(
            pomModel.getBuild().getSourceDirectory()
        ).orElse(
            sourceFolderFromFile()
        );
    }

    private String sourceFolderFromFile() {
        String path = file.getAbsolutePath();
        return path.substring(0, path.length() - 7)
            + "src/main/java";
    }
}
