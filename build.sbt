name := "crosstalk"

version := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "org.apache.lucene" % "lucene-core" % "4.9.0"
  ,"org.apache.lucene" % "lucene-analyzers-common" % "4.9.0"
  ,"org.apache.lucene" % "lucene-misc" % "4.9.0"
  ,"org.apache.lucene" % "lucene-highlighter" % "4.9.0"
  ,"org.apache.lucene" % "lucene-suggest" % "4.9.0"
  ,"org.apache.lucene" % "lucene-facet" % "4.9.0"
//  ,"com.fasterxml.jackson.core" % "jackson-core" % "2.4.1"
//  ,"com.fasterxml.jackson.core" % "jackson-annotations" % "2.4.1"
//  ,"com.fasterxml.jackson.core" % "jackson-databind" % "2.4.1"
)     

play.Project.playJavaSettings
