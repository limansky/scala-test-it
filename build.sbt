lazy val scalaTestIt = (project in file("."))
  .aggregate(scalatest, sbtPlugin)
  .settings(
    publish / skip  := true,
    publish         := (()),
    publishLocal    := (()),
    publishArtifact := false,
    publishTo       := None
  )

lazy val scalatest = (project in file("scalatest"))
  .settings(
    name               := "scala-test-it",
    scalaVersion       := versions.scala213,
    crossScalaVersions := Seq(versions.scala212, versions.scala213, versions.scala3),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest-core"      % versions.scalatest,
      "ru.testit"      % "testit-java-commons" % versions.testIt
    )
  )

lazy val sbtPlugin = (project in file("sbt-plugin"))
  .settings(
    name                          := "sbt-test-it",
    scalaVersion                  := versions.scala212,
    crossScalaVersions            := List(versions.scala212, versions.scala3sbt),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.2.8"
        case _      => "2.0.0-RC4"
      }
    },
    buildInfoKeys                 := Seq(version),
    buildInfoPackage              := "me.limansky.sbttestit"
  )
  .enablePlugins(SbtPlugin, BuildInfoPlugin)

lazy val versions = new {
  val scala212  = "2.12.20"
  val scala213  = "2.13.16"
  val scala3    = "3.3.6"
  val scala3sbt = "3.7.2"

  val scalatest = "3.2.19"
  val testIt    = "2.7.10"
}

ThisBuild / licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / homepage               := Some(url("http://github.com/limansky/scala-test-it"))
ThisBuild / organization           := "me.limansky"
ThisBuild / organizationName       := "Mike Limansky"
ThisBuild / startYear              := Some(2025)
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
ThisBuild / publishTo              := {
  if (isSnapshot.value) Some(Resolver.sonatypeCentralSnapshots)
  else localStaging.value
}

ThisBuild / scalacOptions := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, _)) => List("-deprecation", "-unchecked", "-feature", "-Xlint")
    case _            => List("-deprecation", "-unchecked", "-feature")
  }
}
