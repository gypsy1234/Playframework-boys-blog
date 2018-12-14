name := """playframework-boys-blog"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.3"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3"

// https://mvnrepository.com/artifact/com.typesafe.slick/slick-hikaricp
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3"

// https://mvnrepository.com/artifact/org.postgresql/postgresql
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.5"

// https://mvnrepository.com/artifact/org.springframework.security/spring-security-web
libraryDependencies += "org.springframework.security" % "spring-security-web" % "5.1.0.RELEASE"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-nop
libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.25" % Test

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.1.0"



// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
