package com.emnify.lint.maven;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import java.io.FileInputStream;
import java.util.List;

public class MavenParser {

  public static void main(String[] args) throws Exception{
    MavenXpp3Reader reader = new MavenXpp3Reader();
    Model model = reader.read(new FileInputStream("/home/danilo/Workspace/AkkaKVStore/kvcluster/pom.xml"));

    System.out.println(model.getGroupId());
    System.out.println(model.getArtifactId());

    List<MavenArtifact> arts = MavenArtifact.fromModel(model).transientDependencies();

    for(MavenArtifact art: arts){
      System.out.println(">> " + art.jarPath());
    }
  }
}
