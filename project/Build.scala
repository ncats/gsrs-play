import sbt._
import Keys._
import play._
import play.PlayImport._

object ApplicationBuild extends Build {

  val branch = "git rev-parse --abbrev-ref HEAD".!!.trim
  val commit = "git rev-parse --short HEAD".!!.trim
  val buildTime = (new java.text.SimpleDateFormat("yyyyMMdd-HHmmss"))
    .format(new java.util.Date())
  val appVersion = "%s-%s-%s".format(branch, commit, buildTime)

  val commonDependencies = Seq(
    javaWs,
    javaJdbc,
    javaEbean,
    cache,
    "mysql" % "mysql-connector-java" % "5.1.31"
      ,"commons-codec" % "commons-codec" % "1.9"
      ,"org.apache.lucene" % "lucene-core" % "4.10.0"
      ,"org.apache.lucene" % "lucene-analyzers-common" % "4.10.0"
      ,"org.apache.lucene" % "lucene-misc" % "4.10.0"
      ,"org.apache.lucene" % "lucene-highlighter" % "4.10.0"
      ,"org.apache.lucene" % "lucene-suggest" % "4.10.0"
      ,"org.apache.lucene" % "lucene-facet" % "4.10.0"
      ,"org.apache.lucene" % "lucene-queryparser" % "4.10.0"
      ,"org.webjars" %% "webjars-play" % "2.3.0"
      ,"org.webjars" % "bootstrap" % "3.3.5"
      ,"org.webjars" % "typeaheadjs" % "0.10.5-1"
      ,"org.webjars" % "handlebars" % "2.0.0-1"
      ,"org.webjars" % "jquery-ui" % "1.11.2"
      ,"org.webjars" % "jquery-ui-themes" % "1.11.2"
      ,"org.webjars" % "angular-ui-bootstrap" % "0.11.0-2"
      ,"org.webjars" % "font-awesome" % "4.2.0"
      ,"org.webjars" % "html5shiv" % "3.7.2"
      ,"org.webjars" % "requirejs" % "2.1.15"
      ,"org.webjars" % "respond" % "1.4.2"
      ,"org.webjars" % "highcharts" % "4.0.4"
      ,"org.webjars" % "highslide" % "4.1.13"
      ,"org.webjars" % "html2canvas" % "0.4.1"
      ,"org.reflections" % "reflections" % "0.9.8" notTransitive ()
      ,"colt" % "colt" % "1.2.0"
      //,"net.sf.jni-inchi" % "jni-inchi" % "0.8"
      ,"org.freehep" % "freehep-graphicsbase" % "2.4"
      ,"org.freehep" % "freehep-vectorgraphics" % "2.4"
      ,"org.freehep" % "freehep-graphicsio" % "2.4"
      ,"org.freehep" % "freehep-graphicsio-svg" % "2.4"
      ,"org.freehep" % "freehep-graphics2d" % "2.4"
      //,"ws.securesocial" %% "securesocial" % "master-SNAPSHOT"
      ,"org.webjars" % "fabric.js" % "1.4.12"
      ,"org.webjars.bower" % "spin.js" % "2.0.2"
  )

  val scalaBuildOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:reflectiveCalls",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:dynamics",
    "-language:higherKinds",
    "-language:existentials",
    "-language:experimental.macros"
  )

  val javaBuildOptions = Seq(
    "-encoding", "UTF-8"
      ,"-Xlint:-options"
      //,"-Xlint:deprecation"
  )

  val core = Project("core", file("."))
    .enablePlugins(PlayJava).settings(
    version := appVersion,
      libraryDependencies ++= commonDependencies,
      javacOptions ++= javaBuildOptions
  )

  val ncats = Project("ncats", file("modules/ncats"))
    .enablePlugins(PlayJava).settings(
    version := appVersion,
      libraryDependencies ++= commonDependencies,
      javacOptions ++= javaBuildOptions
        //javaOptions in Runtime += "-Dconfig.resource=ncats.conf"
  ).dependsOn(core).aggregate(core)

  // needs to specify on the commandline during development and dist
  //  sbt -Dconfig.file=modules/granite/conf/granite.conf granite/run
  val granite = Project("granite", file("modules/granite"))
    .enablePlugins(PlayJava).settings(
    version := appVersion,
      libraryDependencies ++= commonDependencies,
      javacOptions ++= javaBuildOptions
      //javaOptions in Runtime += "-Dconfig.resource=granite.conf"
  ).dependsOn(ncats).aggregate(ncats)

  val idg = Project("idg", file("modules/idg"))
    .enablePlugins(PlayJava).settings(
    version := appVersion,
      libraryDependencies ++= commonDependencies,
      javacOptions ++= javaBuildOptions
      //javaOptions in Runtime += "-Dconfig.resource=pharos.conf"
  ).dependsOn(ncats).aggregate(ncats)

  val ginas = Project("ginas", file("modules/ginas"))
    .enablePlugins(PlayJava).settings(
    version := appVersion,
      libraryDependencies ++= commonDependencies,
      libraryDependencies += "org.webjars" % "dojo" % "1.10.0",
      javacOptions ++= javaBuildOptions
  ).dependsOn(ncats).aggregate(ncats)

  val hcs = Project("hcs", file("modules/hcs"))
    .enablePlugins(PlayJava).settings(
    version := appVersion,
      libraryDependencies ++= commonDependencies,
      javacOptions ++= javaBuildOptions
  ).dependsOn(ncats).aggregate(ncats)

  val srs = Project("srs", file("modules/srs"))
    .enablePlugins(PlayJava).settings(
    version := appVersion,
      libraryDependencies ++= commonDependencies,
      javacOptions ++= javaBuildOptions
  ).dependsOn(ncats).aggregate(ncats)

  val reach = Project("reach", file("modules/reach"))
    .enablePlugins(PlayJava).settings(
    version := appVersion,
      libraryDependencies ++= commonDependencies,
      javacOptions ++= javaBuildOptions
  ).dependsOn(ncats).aggregate(ncats)

  val qhts = Project("qhts", file("modules/qhts"))
    .enablePlugins(PlayJava).settings(
    version := appVersion,
      libraryDependencies ++= commonDependencies,
      javacOptions ++= javaBuildOptions
  ).dependsOn(ncats).aggregate(ncats)

  val tox21 = Project("tox21", file("modules/tox21"))
    .enablePlugins(PlayJava).settings(
    version := appVersion,
      libraryDependencies ++= commonDependencies,
      javacOptions ++= javaBuildOptions
  ).dependsOn(qhts).aggregate(qhts)

}
