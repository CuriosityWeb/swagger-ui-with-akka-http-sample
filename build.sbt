name := "swagger-ui-with-akka-http-sample"
organization := "com.github.CuriosityWeb"
version := "1.0.0"

scalaVersion := "2.13.8"

val AkkaVersion = "2.6.20"
val AkkaHttpVersion = "10.2.10"
val SlickVersion = "3.4.0"

libraryDependencies ++=
  Seq("com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion)

libraryDependencies ++=
  Seq("com.typesafe.slick" %% "slick" % SlickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion)

libraryDependencies ++=
  Seq("org.slf4j" % "slf4j-simple" % "1.7.36",
    "com.h2database" % "h2" % "2.1.214",
    "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.8.0",
    "jakarta.ws.rs" % "jakarta.ws.rs-api" % "3.1.0")
