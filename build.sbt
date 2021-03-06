name := """shop"""
organization := "com.example"

//version := "1.0-SNAPSHOT"
//
//lazy val root = (project in file(".")).enablePlugins(PlayScala)
//
//scalaVersion := "2.13.5"
//
//libraryDependencies += guice
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
//libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0"
//libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
//libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.21.0"
//libraryDependencies += "com.mohiva" %% "play-silhouette" % "6.1.1"
//libraryDependencies += "com.mohiva" %% "play-silhouette-password-bcrypt" % "6.1.1"
//libraryDependencies += "com.mohiva" %% "play-silhouette-persistence" % "6.1.1"
//libraryDependencies += "com.mohiva" %% "play-silhouette-crypto-jca" % "6.1.1"
//libraryDependencies += "com.mohiva" %% "play-silhouette-totp" % "6.1.1"
//libraryDependencies += "net.codingwell" %% "scala-guice" % "4.2.6"
//libraryDependencies += "com.iheart" %% "ficus" % "1.4.7"

version := "1.0"

lazy val `e_biznes_backend` = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.jcenterRepo

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(ehcache, ws, specs2 % Test, guice)
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0",
  "org.xerial" % "sqlite-jdbc" % "3.30.1",
  "com.iheart" %% "ficus" % "1.4.7",
  "com.mohiva" %% "play-silhouette" % "6.1.1",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "6.1.1",
  "com.mohiva" %% "play-silhouette-persistence" % "6.1.1",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "6.1.1",
  "com.mohiva" %% "play-silhouette-totp" % "6.1.1",
  "net.codingwell" %% "scala-guice" % "4.2.6"
)
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

//resolvers += Resolver.jcenterRepo
//
//resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
