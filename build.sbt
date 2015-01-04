name := "inxight"

version := "0.0.1-idg"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

//lazy val admin = (project in file("modules/admin")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaWs,
  javaJdbc,
  javaEbean,
  cache,
  "commons-codec" % "commons-codec" % "1.3"
  ,"org.apache.lucene" % "lucene-core" % "4.10.0"
  ,"org.apache.lucene" % "lucene-analyzers-common" % "4.10.0"
  ,"org.apache.lucene" % "lucene-misc" % "4.10.0"
  ,"org.apache.lucene" % "lucene-highlighter" % "4.10.0"
  ,"org.apache.lucene" % "lucene-suggest" % "4.10.0"
  ,"org.apache.lucene" % "lucene-facet" % "4.10.0"
  ,"org.apache.lucene" % "lucene-queryparser" % "4.10.0"
  ,"org.webjars" %% "webjars-play" % "2.3.0"
  ,"org.webjars" % "bootstrap" % "3.2.0"
  ,"org.webjars" % "angular-ui-bootstrap" % "0.11.0-2"
  ,"mysql" % "mysql-connector-java" % "5.1.31"
//  ,"com.wordnik" %% "swagger-play2" % "1.3.10" exclude("org.reflections", "reflections")
  ,"org.reflections" % "reflections" % "0.9.8" notTransitive ()
//  ,"com.fasterxml.jackson.core" % "jackson-core" % "2.4.1"
//  ,"com.fasterxml.jackson.core" % "jackson-annotations" % "2.4.1"
//  ,"com.fasterxml.jackson.core" % "jackson-databind" % "2.4.1"
)     

