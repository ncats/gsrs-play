import play._
import sbt.Keys._
import sbt._
//import play.PlayImport._
import play.Play.autoImport._

object ApplicationBuild extends Build {
  val branch = "git rev-parse --abbrev-ref HEAD".!!.trim
  val commit = "git rev-parse --short HEAD".!!.trim
  val author = s"git show --format=%an -s $commit".!!.trim
  val buildDate = (new java.text.SimpleDateFormat("yyyyMMdd"))
    .format(new java.util.Date())
  val appVersion = "%s-%s-%s".format(branch, buildDate, commit)

  val commonSettings = Seq(
    version := appVersion,    
    scalaVersion := "2.11.7",
//    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.10.5",
//      "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4",
//      "2.11.5", "2.11.6", "2.11.7"),
//    resolvers += Resolver.typesafeRepo("releases"),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    resolvers += Resolver.url("Edulify Repository",
        url("https://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)
  )
  
  val commonDependencies = Seq(
    javaWs,
    javaJdbc,
    javaEbean,
    cache,
    filters,

    "org.apache.httpcomponents" % "httpclient" %"4.5.1",
    "org.apache.httpcomponents" % "httpcore" %"4.4.4",
    "org.apache.httpcomponents" % "httpclient" %"4.3.1", //required for Ivy bug?
    "commons-io" % "commons-io" % "2.4",

    "net.sourceforge.htmlunit" % "htmlunit" % "2.20" % Test,
    "com.zaxxer" % "HikariCP" % "2.4.6"
      ,"com.edulify" %% "play-hikaricp" % "2.1.0"
      ,"mysql" % "mysql-connector-java" % "5.1.31"
      ,"org.postgresql" % "postgresql" % "9.4-1201-jdbc41"     
      ,"com.hazelcast" % "hazelcast" % "3.5.2" 
      ,"org.julienrf" %% "play-jsonp-filter" % "1.2"
      ,"commons-codec" % "commons-codec" % "1.9"
      ,"org.apache.lucene" % "lucene-core" % "4.10.0"
      ,"org.apache.lucene" % "lucene-analyzers-common" % "4.10.0"
      ,"org.apache.lucene" % "lucene-misc" % "4.10.0"
      ,"org.apache.lucene" % "lucene-highlighter" % "4.10.0"
      ,"org.apache.lucene" % "lucene-suggest" % "4.10.0"
      ,"org.apache.lucene" % "lucene-facet" % "4.10.0"
      ,"org.apache.lucene" % "lucene-queryparser" % "4.10.0"
      ,"com.github.fge" % "json-patch" % "1.9"
      ,"org.quartz-scheduler" % "quartz" % "2.2.1"
      ,"org.webjars" %% "webjars-play" % "2.3.0"
      ,"org.webjars" % "bootstrap" % "3.3.5"
      ,"org.webjars" % "typeaheadjs" % "0.10.5-1"
      ,"org.webjars" % "handlebars" % "2.0.0-1"
      ,"org.webjars" % "jquery-ui" % "1.11.2"
      ,"org.webjars" % "jquery-ui-themes" % "1.11.2"
      ,"org.webjars" % "font-awesome" % "4.5.0"
      ,"org.webjars" % "html5shiv" % "3.7.2"
      ,"org.webjars" % "requirejs" % "2.1.15"
      ,"org.webjars" % "respond" % "1.4.2"
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
      ,"org.webjars.bower" % "spin.js" % "2.0.2"
      ,"be.objectify" %% "deadbolt-java" % "2.3.3"
      ,"com.sleepycat" % "je" % "5.0.73"
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
    "-encoding", "UTF-8",
    "-source", "1.8",
    "-target", "1.8"
  )
  val javaDocOptions = Seq(
    "-encoding", "UTF-8",
    "-source", "1.8"
  ) 


  val build = Project("build", file("modules/build"))
    .settings(commonSettings:_*).settings(
    sourceGenerators in Compile <+= sourceManaged in Compile map { dir =>
      val file = dir / "BuildInfo.java"
      IO.write(file, """
package ix;
public class BuildInfo { 
   public static final String BRANCH = "%s";
   public static final String DATE = "%s";
   public static final String COMMIT = "%s";
   public static final String TIME = "%s";
   public static final String AUTHOR = "%s";
}
""".format(branch, buildDate, commit, new java.util.Date(), author))
      Seq(file)
    }
  )

  val seqaln = Project("seqaln", file("modules/seqaln"))
    .settings(commonSettings:_*).settings(
    libraryDependencies ++= commonDependencies,
    javacOptions in (Compile, compile) ++= javaBuildOptions,
    javacOptions in (doc) ++= javaDocOptions,
    mainClass in (Compile,run) := Some("ix.seqaln.SequenceIndexer")
  )

  val core = Project("core", file("."))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
  ).dependsOn(build,seqaln).aggregate(build,seqaln)

  val ncats = Project("ncats", file("modules/ncats"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
        //javaOptions in Runtime += "-Dconfig.resource=ncats.conf"
  ).dependsOn(core).aggregate(core)

  val marvin = Project("marvin", file("modules/marvin"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
    libraryDependencies ++= commonDependencies
  ).dependsOn(ncats).aggregate(ncats)
  
  // needs to specify on the commandline during development and dist
  //  sbt -Dconfig.file=modules/granite/conf/granite.conf granite/run
  val granite = Project("granite", file("modules/granite"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
      //javaOptions in Runtime += "-Dconfig.resource=granite.conf"
  ).dependsOn(ncats).aggregate(ncats)

  val idg = Project("idg", file("modules/idg"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
    libraryDependencies ++= commonDependencies,
      libraryDependencies += "org.webjars" % "morrisjs" % "0.5.1",
      libraryDependencies += "org.webjars" % "fabric.js" % "1.4.12",
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
      //javaOptions in Runtime += "-Dconfig.resource=pharos.conf"
  ).dependsOn(marvin).aggregate(marvin)


  val ginasTestOptions = "-Dconfig.file=" + Option(System.getProperty("testconfig")).getOrElse("application.conf")
  val ginas = Project("ginas", file("modules/ginas"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      libraryDependencies += "org.webjars" % "angularjs" % "1.5.0",
      libraryDependencies += "org.webjars" % "angular-ui-bootstrap" % "1.3.3",
      libraryDependencies += "org.webjars" % "dojo" % "1.10.0",
      libraryDependencies += "org.webjars" % "momentjs" % "2.11.0",
      libraryDependencies += "org.webjars" % "lodash" % "4.0.0",
      libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.2" % Test,
      libraryDependencies  += "junit" % "junit" % "4.12" % Test,
      libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,

	  javaOptions ++= Seq("-Xmx4096M", "-Xms512M", "-XX:MaxPermSize=2048M"),
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions,

    javaOptions in Test += ginasTestOptions,
    cleanFiles += file("modules/ginas/ginas.ix")
  ).dependsOn(ncats).aggregate(ncats)


  val hcs = Project("hcs", file("modules/hcs"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
  ).dependsOn(ncats).aggregate(ncats)

  val moldev = Project("moldev", file("modules/moldev"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      libraryDependencies += "org.apache.poi" % "poi" % "3.13",
        javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
    ).dependsOn(ncats).aggregate(ncats)

  val srs = Project("srs", file("modules/srs"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
  ).dependsOn(ncats).aggregate(ncats)

  val reach = Project("reach", file("modules/reach"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      libraryDependencies +="org.webjars" % "highcharts" % "4.0.4",
      libraryDependencies +="org.webjars" % "highslide" % "4.1.13",
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
  ).dependsOn(ncats).aggregate(ncats)

  val qhts = Project("qhts", file("modules/qhts"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
  ).dependsOn(marvin).aggregate(marvin)

  val tox21 = Project("tox21", file("modules/tox21"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
  ).dependsOn(qhts).aggregate(qhts)

  val ntd = Project("ntd", file("modules/ntd"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
      //javaOptions in Runtime += "-Dconfig.resource=pharos.conf"
    ).dependsOn(ncats).aggregate(ncats)

  val cbc = Project("cbc", file("modules/cbc"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
  ).dependsOn(ncats).aggregate(ncats)

  val ginasEvo = Project("ginas-evolution", file("modules/ginas-evolution"))
    .settings(commonSettings: _*).settings(
    libraryDependencies ++= commonDependencies,
      libraryDependencies += "com.typesafe" % "config" % "1.2.0",
      mainClass in (Compile,run) := Some("ix.ginas.utils.Evolution")
  ).dependsOn(ginas).aggregate(ginas)
}
