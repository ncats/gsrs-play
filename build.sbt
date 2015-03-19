name := "inxight"

version := "0.0.6-tox21"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

//lazy val admin = (project in file("modules/admin")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaWs,
  javaJdbc,
  javaEbean,
  cache,
  "com.typesafe.akka" % "akka-cluster_2.11" % "2.3.4"
    //,"com.typesafe.akka" % "akka-docs_2.11" % "2.3.9"
    ,"mysql" % "mysql-connector-java" % "5.1.31"
    ,"commons-codec" % "commons-codec" % "1.3"
    ,"org.apache.lucene" % "lucene-core" % "4.10.0"
    ,"org.apache.lucene" % "lucene-analyzers-common" % "4.10.0"
    ,"org.apache.lucene" % "lucene-misc" % "4.10.0"
    ,"org.apache.lucene" % "lucene-highlighter" % "4.10.0"
    ,"org.apache.lucene" % "lucene-suggest" % "4.10.0"
    ,"org.apache.lucene" % "lucene-facet" % "4.10.0"
    ,"org.apache.lucene" % "lucene-queryparser" % "4.10.0"
    ,"org.webjars" %% "webjars-play" % "2.3.0"
    ,"org.webjars" % "bootstrap" % "3.2.0"
    ,"org.webjars" % "typeaheadjs" % "0.10.5-1"
    ,"org.webjars" % "handlebars" % "2.0.0-1"
    ,"org.webjars" % "jquery-ui" % "1.11.2"
    ,"org.webjars" % "jquery-ui-themes" % "1.11.2"
    ,"org.webjars" % "angular-ui-bootstrap" % "0.11.0-2"
    ,"org.webjars" % "metroui" % "2.0.23"
    ,"org.webjars" % "font-awesome" % "4.2.0"
    ,"org.webjars" % "html5shiv" % "3.7.2"
    ,"org.webjars" % "requirejs" % "2.1.15"
    ,"org.webjars" % "respond" % "1.4.2"
      ,"org.webjars" % "highcharts" % "4.0.4"
  ,"org.webjars" % "highslide" % "4.1.13"
    ,"org.reflections" % "reflections" % "0.9.8" notTransitive ()
//  ,"com.wordnik" %% "swagger-play2" % "1.3.10" exclude("org.reflections", "reflections")
)     

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)
