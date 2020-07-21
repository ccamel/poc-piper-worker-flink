ThisBuild / resolvers ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

name := "piper-worker-flink"

version := "0.1-SNAPSHOT"

organization := "com.ccm.me.piper.worker.flink"

ThisBuild / scalaVersion := "2.11.12"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % "1.11.1" % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % "1.11.1" % "provided",
  "org.apache.flink" %% "flink-connector-rabbitmq" % "1.11.1",
  "org.slf4j" % "slf4j-log4j12" % "1.7.30" % "runtime",
  "log4j" % "log4j" % "1.2.17" % "runtime")

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies
  )

assembly / mainClass := Some("com.ccm.me.piper.worker.flink.Job")

Compile / run := Defaults.runTask(Compile / fullClasspath,
  Compile / run / mainClass,
  Compile / run / runner
).evaluated

Compile / run / fork := true
Global / cancelable := true

assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)
