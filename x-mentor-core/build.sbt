name := """x-mentor-core"""
organization := "io.github.serdeliverance"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"

lazy val circeVersion = "0.12.2"

libraryDependencies ++= Seq(
  guice,
  ws,
  // Logs
  "net.logstash.logback" % "logstash-logback-encoder" % "5.1",
  // Json
  "com.dripower" %% "play-circe"           % "2812.0",
  "io.circe"     %% "circe-core"           % circeVersion,
  "io.circe"     %% "circe-generic"        % circeVersion,
  "io.circe"     %% "circe-parser"         % circeVersion,
  "io.circe"     %% "circe-generic-extras" % circeVersion,
  // Functional Programming
  "org.typelevel" %% "cats-core"   % "2.1.1",
  "org.typelevel" %% "cats-effect" % "2.1.2",
  // Redis
  "redis.clients" % "jedis" % "3.1.0",
  "com.redislabs" % "jredisgraph" % "2.2.0",
  "com.redislabs" % "jrebloom" % "2.1.0",
  "com.redislabs" % "jredistimeseries" % "1.4.0",
  "com.redislabs" % "jredisai" % "0.9.0",

  // Test
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
)

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
)
