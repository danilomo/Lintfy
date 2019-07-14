package com.emnify.lint.maven;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public abstract class MavenArtifact {
  private static final String HOME_FOLDER = System.getProperty("user.home");

    public static MavenArtifact fromModel(Model model) {
        return new MavenArtifact() {
            @Override
            public String groupId() {
                return model.getGroupId();
            }

            @Override
            public String artifactId() {
                return model.getArtifactId();
            }

            @Override
            public String version() {
                return model.getVersion();
            }

            @Override
            public List<MavenArtifact> dependencies() {
                List<MavenArtifact> result = model.getDependencies()
                    .stream()
                    .map(MavenArtifact::fromDependency)
                    .collect(Collectors.toList());
                parent().ifPresent(
                    parent -> result.addAll(parent.dependencies())
                );
                return result;
            }

            @Override
            public Optional<MavenArtifact> parent() {
                return Optional.ofNullable(
                    model.getParent()
                ).map(MavenArtifact::fromParent);
            }
        };
    }

    public static MavenArtifact fromDependency(Dependency dependency) {
        return new MavenArtifact() {
            @Override
            public String groupId() {
                return dependency.getGroupId();
            }

            @Override
            public String artifactId() {
                return dependency.getArtifactId();
            }

            @Override
            public String version() {
                return dependency.getVersion();
            }
        };
    }

    public static MavenArtifact fromParent(Parent parent) {
        return new MavenArtifact() {
            @Override
            public String groupId() {
                return parent.getGroupId();
            }

            @Override
            public String artifactId() {
                return parent.getArtifactId();
            }

            @Override
            public String version() {
                return parent.getVersion();
            }
        };
    }

  public String jarPath(){
    return String.join(
        "/",
        baseFolder(),
        jarFileName()
    );
  }

  public String pomPath(){
    return String.join(
        "/",
        baseFolder(),
        pomName()
    );
  }

    public abstract String groupId();

    public abstract String artifactId();

    public abstract String version();

    public List<MavenArtifact> dependencies() {
        try {
            Model model = modelFrom(pomPath());
            return model.getDependencies()
                .stream()
                .map(MavenArtifact::fromDependency)
                .collect(Collectors.toList());
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private String baseFolder() {
    return String.join(
        "/",
        HOME_FOLDER,
        ".m2/repository",
        groupId().replace(".", "/"),
        artifactId(),
        version()
    );
  }

  private String  jarFileName() {
      return artifactId() + "-" + version() + ".jar";
  }

  private String pomName() {
      return artifactId() + "-" + version() + ".pom";
  }

    private Model modelFrom(String fileName) {
    try {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        return reader.read(new FileInputStream(fileName));
    }catch(Exception ex){
        throw new RuntimeException("Error!");
    }
  }

    public Optional<MavenArtifact> parent() {
        return Optional.empty();
    }

    public List<MavenArtifact> transientDependencies() {
    Map<String, MavenArtifact> map = new HashMap<>();
    findTransientDependencies(this, map);
    return new ArrayList<>(map.values());
  }

  private void findTransientDependencies(MavenArtifact artifact, Map<String,
      MavenArtifact> map){
    for(MavenArtifact dependency: artifact.dependencies()){
        if (!map.containsKey(dependency.artifactId())) {
            map.put(dependency.artifactId(), dependency);
        findTransientDependencies(dependency, map);
      }
    }
  }

    @Override
    public String toString() {
        return "MavenArtifact[" +
            groupId() + ", " +
            artifactId() + ", " +
            version() + "]";
    }
}
