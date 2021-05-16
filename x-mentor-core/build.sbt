name := """x-mentor-core"""
organization := "xmentor"
version := "0.1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala, sbtdocker.DockerPlugin)
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
  "com.redislabs" % "jrejson" % "1.3.0",
  "com.redislabs" % "jredisearch" % "2.0.0",
  //JWT
  "com.pauldijou" %% "jwt-core" % "4.2.0",
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

// imageName:Tag value
imageNames in docker := Seq(
  ImageName(s"${organization.value}/${name.value}:latest")
)

dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = "/opt/docker"

  new Dockerfile {
    from("openjdk:8-jre-slim")
    expose(9000)
    run("apt", "update")
    run("apt", "-y", "upgrade")
    run("apt", "-y", "install", "curl")
    copy(appDir, targetDir)
    run("chmod", "-R", "755", s"$targetDir")
    entryPoint(s"$targetDir/conf/wrapper.sh")
  }
}