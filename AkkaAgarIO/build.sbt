ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"
resolvers += "Akka library repository".at("https://repo.akka.io/maven")
lazy val akkaVersion = "2.10.5"

lazy val root = (project in file("."))
  .settings(
    name := "agar-io",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "ch.qos.logback" % "logback-classic" % "1.5.18",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.8.8"
    ),
    Compile / run / fork := true,
    runServer := {
      val oldOpts = (Compile / run / javaOptions).value
      (Compile / run / javaOptions) := oldOpts ++ Seq("-Dconfig.resource=agario-server.conf")
      (Compile / runMain).toTask(" it.unibo.agar.controller.ServerMain").value
    },
    runClient := {
      val oldOpts = (Compile / run / javaOptions).value
      (Compile / run / javaOptions) := oldOpts ++ Seq("-Dconfig.resource=agario-client.conf")
      (Compile / runMain).toTask(" it.unibo.agar.controller.ClientMain").value
    }
  )

lazy val runServer = taskKey[Unit]("Run the server")
lazy val runClient = taskKey[Unit]("Run the client")
