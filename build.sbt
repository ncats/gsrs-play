name := "inxight"

version := "0.0.1"

lazy val test = (project in file("modules/test"))
  .enablePlugins(PlayJava)

lazy val main = (project in file(".")).enablePlugins(PlayJava)
  .aggregate(test)

//lazy val admin = (project in file("modules/admin")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Common.libraries

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

javacOptions ++= Seq(
  "-source", "1.7",
  "-target", "1.7",
  "-encoding",
  "UTF-8",  "-Xlint:-options"
)

resolvers += Resolver.sonatypeRepo("snapshots")
