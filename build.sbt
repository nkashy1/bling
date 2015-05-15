organization := "systems.adaptix"

name := "bling"

version := "1.0"

scalaVersion := "2.11.6"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.6" % "test",
  "com.typesafe" % "config" % "1.0.2",
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.zaxxer" % "HikariCP-java6" % "2.3.7",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
)
