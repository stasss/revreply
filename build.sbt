name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies += jdbc
libraryDependencies += cache
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
libraryDependencies += "com.google.apis" % "google-api-services-androidpublisher" % "v2-rev47-1.23.0"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0" classifier "models"


libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.264"
libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1212"




