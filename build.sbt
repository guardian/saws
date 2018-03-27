scalaVersion := "2.12.4"

name := "saws"
organization := "com.gu"

val awsSdkVersion = "1.11.258"
val slf4jVersion = "1.7.24"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-cloudformation" % awsSdkVersion,
  "com.amazonaws" % "aws-java-sdk-autoscaling" % awsSdkVersion,
  "com.github.scopt" %% "scopt" % "3.7.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)

scalacOptions := Seq("-unchecked", "-deprecation")
assemblyJarName in assembly := "saws.jar"
