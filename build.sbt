lazy val scalalib = ProjectRef(uri("https://github.com/lichess-org/scalalib.git#fb570ef"), "core")
lazy val scalachess = ProjectRef(uri("https://github.com/lichess-org/scalachess.git#f11e189"), "scalachess")
lazy val publishScalalib = taskKey[Unit]("Publish scalalib dependency to local Maven repository")
lazy val publishScalachess = taskKey[Unit]("Publish scalachess dependency to local Maven repository")

publishScalalib := {
  val depDir = (LocalRootProject / baseDirectory).value / "target" / "scalalib"

  (scalalib / publishM2).value
  IO.delete(depDir)
}

publishScalachess := {
  val depDir = (LocalRootProject / baseDirectory).value / "target" / "scalachess"

  (scalachess / publishM2).value
  IO.delete(depDir)
}

scalalib / isSnapshot := true
scalalib / publishMavenStyle := true
scalachess / isSnapshot := true
scalachess / publishMavenStyle := true

publishM2 := {
  publishScalalib.value
  publishScalachess.value
  publishM2.value
}

lazy val scalachessWrapper = project.in(file("."))
  .withId("scalachess-wrapper")
  .settings(
    name := "scalachess-wrapper",
    organization := "com.playchess2earn",
    version := "0.0.1",
    scalaVersion := "3.8.3",
    licenses += ("MIT" -> url("https://opensource.org/licenses/MIT")),
    semanticdbEnabled := true,
    Compile / packageDoc / publishArtifact := false,
    versionScheme := Some("semver-spec"),
    isSnapshot := true,
    publishMavenStyle := true,
    publishTo := Some(Resolver.file("file", file(sys.props("user.home")) / ".m2" / "repository")),
    scalacOptions := Seq(
      "-encoding",
      "utf-8",
      "-feature",
      "-language:postfixOps",
      "-Wunused:all",
      "-release:21"
    )
  )
  .dependsOn(scalalib, scalachess)

