package com.emnify.lint.maven;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MavenArtifact {
  private static final String HOME_FOLDER = System.getProperty("user.home");

  public abstract String getGroupId();

  public abstract String getArtifactId();

  public abstract String getVersion();

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

  private String baseFolder() {
    return String.join(
        "/",
        HOME_FOLDER,
        ".m2/repository",
        getGroupId().replace(".", "/"),
        getArtifactId(),
        getVersion()
    );
  }

  private String  jarFileName() {
    return getArtifactId() + "-" + getVersion() + ".jar";
  }

  private String pomName() {
    return getArtifactId() + "-" + getVersion() + ".pom";
  }

  public List<MavenArtifact> dependencies(){
    try {
      Model model = modelFrom(pomPath());
      return model.getDependencies()
          .stream()
          .map(MavenArtifact::fromDependency)
          .collect(Collectors.toList());
    }catch(Exception ex){
      return new ArrayList<>();
    }
  }

  public List<MavenArtifact> transientDependencies(){
    Map<String, MavenArtifact> map = new HashMap<>();
    findTransientDependencies(this, map);
    return new ArrayList<>(map.values());
  }

  private void findTransientDependencies(MavenArtifact artifact, Map<String, MavenArtifact> map){
    for(MavenArtifact dependency: artifact.dependencies()){
      if(!map.containsKey(dependency.getArtifactId())){
        map.put(dependency.getArtifactId(), dependency);
        findTransientDependencies(dependency, map);
      }
    }
  }

  private Model modelFrom(String fileName){
    try {
      MavenXpp3Reader reader = new MavenXpp3Reader();
      return reader.read(new FileInputStream(fileName));
    }catch(Exception ex){
      throw new RuntimeException("Error!");
    }
  }

  public static MavenArtifact fromModel(Model model){
    return new MavenArtifact() {
      @Override
      public String getGroupId() {
        return model.getGroupId();
      }

      @Override
      public String getArtifactId() {
        return model.getArtifactId();
      }

      @Override
      public String getVersion() {
        return model.getVersion();
      }

      @Override
      public List<MavenArtifact> dependencies() {
        return model.getDependencies()
            .stream()
            .map(MavenArtifact::fromDependency)
            .collect(Collectors.toList());
      }
    };
  }

  public static MavenArtifact fromDependency(Dependency dependency){
    return new MavenArtifact() {
      @Override
      public String getGroupId() {
        return dependency.getGroupId();
      }

      @Override
      public String getArtifactId() {
        return dependency.getArtifactId();
      }

      @Override
      public String getVersion() {
        return dependency.getVersion();
      }
    };
  }
}
