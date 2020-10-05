import play._
import sbt.Keys._
import sbt.{file, _}
//import play.PlayImport._
import play.Play.autoImport._
import scala.collection.JavaConversions.mapAsScalaMap
import com.typesafe.sbt.SbtNativePackager._
//import NativePackagerKeys._

object ApplicationBuild extends Build {
  val molwitchImplementation = System.getProperty("molwitch", "cdk")
  val displayVersion = "2.6.1"
  val now = new java.util.Date();
  val branch = "git rev-parse --abbrev-ref HEAD".!!.trim
  val commit = "git rev-parse --short HEAD".!!.trim
  val buildDate = (new java.text.SimpleDateFormat("yyyyMMdd"))
    .format(now)
  val buildTime = (new java.text.SimpleDateFormat("HHmmss"))
    .format(now)
  val appVersion = "%s-%s-%s-%s".format(branch, buildDate,buildTime, commit)

  val commonSettings = Seq(
    version := appVersion,    
    scalaVersion := "2.11.7",
//    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.10.5",
//      "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4",
//      "2.11.5", "2.11.6", "2.11.7"),
    resolvers +=
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    resolvers += Resolver.typesafeRepo("releases"),
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

    "org.jcvi.jillion" % "jillion" % "5.3.2",
    "com.fasterxml.jackson.core" % "jackson-core" % "2.9.7",
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.7",
        "com.fasterxml.jackson.core" % "jackson-annotations" % "2.9.7",
//    "com.ning" % "async-http-client" % "1.7.13" % Test,
//    "gov.nih.ncats" % "molwitch" % "0.5",
//    "gov.nih.ncats" % "molwitch-renderer" % "1.0",



//    "gov.nih.ncats" % "molvec" % "0.9.3",
//    "com.twelvemonkeys.imageio" % "imageio-core" % "3.4.1",
//    "com.twelvemonkeys.imageio" % "imageio" % "3.4.1",
//    "com.twelvemonkeys.imageio" % "imageio-tiff" % "3.4.1",
//    "com.twelvemonkeys.imageio" % "imageio-jpeg"% "3.4.1",
    "org.apache.httpcomponents" % "httpclient" %"4.5.2",
    "org.apache.httpcomponents" % "httpcore" %"4.4.4",
    "org.apache.httpcomponents" % "httpclient" %"4.3.1", //required for Ivy bug?
    "commons-io" % "commons-io" % "2.4",
    "com.flipkart.zjsonpatch" % "zjsonpatch" % "0.4.6",
    "javax.xml.bind" % "jaxb-api" % "2.3.0",    //required for JAVA > 9
    "net.sourceforge.htmlunit" % "htmlunit" % "2.35.0" % Test,
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
      ,"org.quartz-scheduler" % "quartz" % "2.2.1"
      ,"org.webjars" %% "webjars-play" % "2.3.0"
      ,"org.webjars" % "bootstrap" % "3.3.7"
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
    ,"uk.ac.cam.ch.opsin" % "opsin-core" % "2.3.1"
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

  val molwitchJchem = file("molwitch-implementations/jchem3");
  val molwitchCDK = file("molwitch-implementations/cdk");

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
   public static final String VERSION = "%s";
}
""".format(branch, buildDate, commit, new java.util.Date(), displayVersion))
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

  val ixdb = Project("ixdb", file("modules/ixdb"))
    .settings(commonSettings:_*).settings(
    libraryDependencies ++= commonDependencies
  )

  val core = Project("core", file("."))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      //libraryDependencies += "com.wordnik" %% "swagger-play2" % "1.3.12",
          libraryDependencies += "com.wordnik" %% "swagger-play2" % "1.3.12" exclude("org.reflections", "reflections"),
          libraryDependencies += "org.reflections" % "reflections" % "0.9.8" notTransitive () ,
          //libraryDependencies += "io.swagger" %% "swagger-play2" % "1.5.1",
          libraryDependencies += "org.webjars" % "swagger-ui" % "2.1.8-M1",
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
  ).dependsOn(build,ixdb,seqaln).aggregate(build,ixdb,seqaln)

  val ncats = Project("ncats", file("modules/ncats"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions
        //javaOptions in Runtime += "-Dconfig.resource=ncats.conf"
  ).dependsOn(core).aggregate(core)

  val ginasEvo = Project("ginas-evolution", file("modules/ginas-evolution"))
    .settings(commonSettings: _*).settings(
    libraryDependencies ++= commonDependencies,
    libraryDependencies += "com.typesafe" % "config" % "1.2.0",
    mainClass in (Compile,run) := Some("ix.ginas.utils.Evolution")
  ).dependsOn(ncats).aggregate(ncats)


  val ginasTestOptions = "-Dconfig.file=" + Option(System.getProperty("config.file")).getOrElse("application.conf")
  val ginas = Project("ginas", file("modules/ginas"))
    .enablePlugins(PlayJava).settings(commonSettings:_*).settings(
      libraryDependencies ++= commonDependencies,
      libraryDependencies += "org.webjars" % "angularjs" % "1.5.7",
      libraryDependencies += "org.webjars" % "angular-ui-bootstrap" % "1.3.3",
      libraryDependencies += "org.webjars" % "dojo" % "1.10.0",
      libraryDependencies += "org.webjars" % "momentjs" % "2.11.0",
      libraryDependencies += "org.webjars.bower" % "later" % "1.2.0",
      libraryDependencies += "org.webjars.bower" % "prettycron" % "0.10.0",
      libraryDependencies += "org.webjars.bower" % "humanize-duration" % "3.0.0",
      libraryDependencies += "org.webjars" % "lodash" % "4.0.0",
      libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.2" % Test,
      libraryDependencies  += "junit" % "junit" % "4.12" % Test,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
    libraryDependencies += "org.apache.poi" % "poi" % "3.17",
    libraryDependencies += "org.apache.poi" % "poi-ooxml" % "3.17",
    libraryDependencies += "org.apache.poi" % "poi-ooxml-schemas" % "3.17",

    libraryDependencies +="pl.joegreen" % "lambda-from-string" % "1.6",
   /* //libraryDependencies += "com.wordnik" %% "swagger-play2" % "1.3.12",
    libraryDependencies += "com.wordnik" %% "swagger-play2" % "1.3.12" exclude("org.reflections", "reflections"),
   // libraryDependencies += "org.reflections" % "reflections" % "0.9.8" notTransitive () ,
    libraryDependencies += "io.swagger" %% "swagger-play2" % "1.5.1",
    libraryDependencies += "org.webjars" % "swagger-ui" % "2.1.8-M1",*/

	  javaOptions ++= Seq("-Xmx4096M", "-Xms512M", "-XX:MaxPermSize=2048M"),
      javacOptions in (Compile, compile) ++= javaBuildOptions,
      javacOptions in (doc) ++= javaDocOptions,

    javaOptions in Test ++= Option(new File("modules/ginas").toPath.relativize(new File(System.getProperty("config.file")).toPath)).map("-Dconfig.file=" + _).toSeq,
    javaOptions in Test ++= mapAsScalaMap(System.getProperties)
                          .filter( prop=> !("config.file".equals(prop._1)) && !("user.dir".equals(prop._1)))
                          .map(prop => s"-D${prop._1}=${prop._2}").toSeq,

    javaOptions in Test ++= Option("-Dmolwitch="+molwitchImplementation).toSeq,
//    javaOptions in Test ++= Seq(Tests.Argument(TestFrameworks.JUnit, "-a"))
    cleanFiles += file("modules/ginas/ginas.ix"),
    //baseDirectory is the ginas module we want to go up a few dirs
    mappings in Universal ++=(baseDirectory.value / "../../cv" * "*" get) map
        (x => x -> ("cv/" + x.getName)),
    //adds evolutions.sh file into the dist
    mappings in Universal += file("evolutions.sh") -> "bin/evolutions.sh",

    unmanagedJars in Compile ++= {
      println("MOLWITCH IMPLEMENTATION = " + molwitchImplementation)
      val path = baseDirectory.value / "../../molwitch-implementations" / molwitchImplementation  / "src";
      println("PATH = " + path);
      val baseDirectories = file( "lib") +++ file( "molwitch-implementations/" +molwitchImplementation +"/jars").getAbsoluteFile
      if(!file( "molwitch-implementations/" +molwitchImplementation +"/jars").isDirectory){
        throw new IllegalArgumentException("molwitch implementation jar directory does not exist! " + path)
      }
      val customJars = (baseDirectories ** "*.jar")

      customJars.classpath
    },

    //    println("MY PATH - " + ),
    unmanagedSourceDirectories in Compile +=  (baseDirectory.value / "../../molwitch-implementations" / molwitchImplementation  / "src").getAbsoluteFile
  ).dependsOn(ginasEvo).aggregate(ginasEvo)
}
