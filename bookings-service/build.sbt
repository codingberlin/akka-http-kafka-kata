import Dependencies._

import sbt.Keys._
import sbt._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "bookings-service",
    scalafmtOnCompile := true,
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.7",
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.20",
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.1",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.mockito" % "mockito-core" % "2.18.3" % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.1.1" % Test,
    Test / testOptions += Tests.Setup { () =>
      println("start docker-compose")
      scala.sys.process.stringToProcess("docker-compose up -d").!
    },
    Test / testOptions += Tests.Cleanup { () =>
      println("stopping docker-compose")
      scala.sys.process.stringToProcess("docker-compose stop").!
    }
  )
