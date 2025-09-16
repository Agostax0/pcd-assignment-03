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
    commands ++= Seq(
      Command.command("runServer") { state =>
        "set run / javaOptions += \"-Dconfig.file=src/main/resources/agario-server.conf\"" ::
          "runMain it.unibo.agar.controller.ServerMain" ::
          "set run / javaOptions := Seq()" ::
          state
      },
      Command.command("runClient") { state =>
        "set run / javaOptions += \"-Dconfig.file=src/main/resources/agario-client.conf\"" ::
          "runMain it.unibo.agar.controller.ClientMain" ::
          "set run / javaOptions := Seq()" ::
          state
      }
    )
  )
