import sbt.Keys._
import sbt._
import play.PlayImport._
import play.PlayScala

object ProjectBuild extends Build {


  resourceGenerators in Compile <+=
    (resourceManaged in Compile, name, version) map { (dir, n, v) =>
      val file = dir / "demo" / "myapp.properties"
      val contents = "name=%s\nversion=%s".format(n,v)
      IO.write(file, contents)
      Seq(file)
    }

  lazy val parent = Project(
    id = "parent",
    base = file("."),
    settings = Defaults.coreDefaultSettings ++ sharedSettings,
    aggregate = Seq(repository, service, ui, ratingImpl)
  ).settings(
    name := "play-social-forum"
  )

  lazy val utils = Project(
    id = "utils",
    base = file("utils"),
    settings = super.settings ++ sharedSettings
  ).settings(
      name := "utils"
    )

  lazy val ui = Project(
    id = "ui",
    base = file("ui"),
    settings = super.settings ++ Seq()
  ).settings(
    libraryDependencies ++= Seq(
      "ws.securesocial" % "securesocial_2.10" % "3.0-M1",
      "com.eaio.uuid" % "uuid" % "3.2",
      "com.wordnik" % "swagger-play2_2.10" % "1.3.12",
      "com.wordnik" % "swagger-play2-utils_2.10" % "1.3.12",
      jdbc,
      filters,
      anorm,
      cache,
      ws
    ),
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    )
  ).enablePlugins(PlayScala) dependsOn (service, ratingApi, ratingImpl % "runtime")

  lazy val service = Project(
    id = "service",
    base = file("service"),
    settings = super.settings ++ sharedSettings
  ).settings(
    name := "service",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"      %% "akka-actor"            % "2.3.4",
      "com.typesafe.akka"      %% "akka-slf4j"            % "2.3.4",
      "org.apache.commons"     %  "commons-email"         % "1.3.3",
      "org.specs2"             %% "specs2"                % "1.14"  % "test",
      "com.typesafe.akka"      %% "akka-testkit"          % "2.3.4" % "test",
      "org.scalamock"    %% "scalamock-scalatest-support" % "3.2.1" % "test"
    )
  ) dependsOn repository

  lazy val repository = Project(
    id = "repository",
    base = file("repository"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
      name := "repository",
      scalaVersion := "2.10.4"
//      libraryDependencies ++= Seq(
//        "org.scalatest" % "scalatest_2.11" % "2.2.5"
//      )
    )

  lazy val ratingApi = Project(
    id = "ratingApi",
    base = file("ratingApi"),
    settings = super.settings ++ sharedSettings
  ).settings(
    name := "ratingApi"
  )

  lazy val ratingImpl = Project(
    id = "ratingImpl",
    base = file("ratingImpl"),
    settings = super.settings ++ sharedSettings
  ).settings(
    name := "ratingImpl",
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-io" % "1.3.2"
    ),
    mappings in (Compile, packageBin) ++= Seq(
      (baseDirectory.value / "exports.txt") -> "META-INF/services/com.eny.rating.Agent"
    )
  ) dependsOn (ratingApi, utils)

  lazy val sharedSettings = super.settings ++ Seq(
    version := "1.0.0",
    scalaVersion := "2.10.4",
    autoCompilerPlugins := true,
    scalacOptions ++= Seq(
      "-language:postfixOps",
      "-language:implicitConversions",
      "-language:reflectiveCalls",
      "-language:higherKinds",
      "-language:existentials",
      "-Yinline-warnings",
      "-Xlint",
      "-deprecation",
      "-feature",
      "-unchecked"
    )
  )
}