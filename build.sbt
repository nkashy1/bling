organization := "systems.adaptix"

name := "bling"

version := "1.0"

scalaVersion := "2.11.6"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.6" % "test",
  "com.typesafe" % "config" % "1.0.2",
  "org.scalikejdbc" %% "scalikejdbc" % "2.2.6",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.2.6",
  "org.scalikejdbc" %% "scalikejdbc-test" % "2.2.6" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.h2database" % "h2" % "1.4.187",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "org.json4s" %% "json4s-native" % "3.2.11"
)

scalacOptions ++= Seq("-Xmax-classfile-name", "255")
