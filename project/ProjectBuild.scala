import sbt.Keys._
import sbt._
import play.PlayImport._
import play.PlayScala

object ProjectBuild extends Build {

  val ScalaVersion = "2.11.7"
  val ScalatestVersion = "3.0.0-M7"
  val ReactiveMongoVersion = "0.11.6"

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
    aggregate = Seq(repository, service, ui, ratingImpl, utils)
  ).settings(
    name := "play-social-forum"
  )

  lazy val utils = Project(
    id = "utils",
    base = file("utils"),
    settings = super.settings ++ sharedSettings
  ).settings(
    name := "utils",
    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.8.2",
      "org.scalatest" % "scalatest_2.11" % ScalatestVersion % "test",
      "commons-lang" % "commons-lang" % "2.2" % "test"
    )
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
      "org.scalatest" % "scalatest_2.11" % ScalatestVersion % "test",
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
      scalaVersion := ScalaVersion
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
    settings = super.settings ++ sharedSettings ++ Seq(
      exportJars := true
    )
  ).settings(
    name := "ratingImpl",
    libraryDependencies ++= Seq(
      "commons-io" % "commons-io" % "2.4"
    ),
    mappings in (Compile, packageBin) ++= Seq(
      (baseDirectory.value / "exports.txt") -> "META-INF/services/com.eny.rating.Agent"
    )
  ) dependsOn (ratingApi, utils)

  lazy val legalEntityApi = Project(
    id = "legalEntityApi",
    base = file("legalEntityApi")
  ).settings(
    name := "legalEntityApi"
  )

  lazy val legalEntityImpl = Project(
    id = "legalEntityImpl",
    base = file("legalEntityImpl")
  ).settings(
    name := "legalEntityImpl",
    libraryDependencies ++= Seq(
      "commons-io" % "commons-io" % "2.4",
      "org.apache.commons" % "commons-lang3" % "3.4",
      "org.reactivemongo" % "reactivemongo_2.11" % ReactiveMongoVersion
    ),
    mappings in (Compile, packageBin) ++= Seq(
      (baseDirectory.value / "exports.txt") -> "META-INF/services/com.eny.rating.Agent"
    )
  ) dependsOn (legalEntityApi, utils)

  lazy val sharedSettings = super.settings ++ Seq(
    version := "1.0.0",
    scalaVersion := ScalaVersion,
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