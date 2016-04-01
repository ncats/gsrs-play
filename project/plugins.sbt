resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.10")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

//addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.2")

//addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "1.0.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.1.0")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5" % "test")

libraryDependencies ++= Seq(
  "org.jacoco" % "org.jacoco.core"   % "0.7.1.201405082137" % "test",
  "org.jacoco" % "org.jacoco.report" % "0.7.1.201405082137" % "test"
)

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.6" % "test")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
