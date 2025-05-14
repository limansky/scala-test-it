lazy val scalatest = (project in file("."))
  .settings(
    name               := "scala-test-it",
    scalaVersion       := versions.scala213,
    crossScalaVersions := Seq(versions.scala212, versions.scala213, versions.scala3),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest"           % versions.scalatest,
      "ru.testit"      % "testit-java-commons" % versions.testIt
    )
  )

lazy val versions = new {
  val scala212 = "2.12.20"
  val scala213 = "2.13.16"
  val scala3   = "3.3.6"

  val scalatest = "3.2.19"
  val testIt    = "2.6.2-TMS-5.3"
}

ThisBuild / licenses += ("Apache 2.0 License", url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / homepage               := Some(url("http://github.com/limansky/scala-test-it"))
ThisBuild / organization           := "me.limansky"
ThisBuild / publishMavenStyle      := true
ThisBuild / Test / publishArtifact := false
ThisBuild / scmInfo                := Some(
  ScmInfo(
    url("https://github.com/limansky/scala-test-it"),
    "scm:git:git@github.com/limansky/scala-test-it.git"
  )
)
ThisBuild / developers             := List(
  Developer("limansky", "Mike Limansky", "mike.limansky@gmail.com", url("http://github.com/limansky"))
)
ThisBuild / publishTo              := sonatypePublishToBundle.value
