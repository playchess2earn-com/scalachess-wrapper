inThisBuild(
  Seq(
    name := "scalachess-wrapper",
    organization := "com.playchess2earn",
    version := "0.0.1",
    scalaVersion := "3.4.1",
    licenses += ("MIT" -> url("https://opensource.org/licenses/MIT")),
    semanticdbEnabled := true,
    Compile / packageDoc / publishArtifact := false,
    versionScheme := Some("semver-spec")
  )
)

val commonSettings = Seq(
  scalacOptions := Seq(
    "-encoding",
    "utf-8",
    "-rewrite",
    "-source:3.4-migration",
    "-feature",
    "-language:postfixOps",
    "-Wunused:all",
    "-release:21"
  )
)

lazy val publishScalalibDependency = taskKey[Unit]("Publish scalalib dependency to local Maven repository")
lazy val publishScalachessDependency = taskKey[Unit]("Publish scalachess dependency to local Maven repository")

publishScalalibDependency := {
  val depDir = (LocalRootProject / baseDirectory).value / "target" / "scalalib"

  publishLocal.dependsOn(scalalib / Compile / publishM2).value
  IO.delete(depDir)
}

publishScalachessDependency := {
  val depDir = (LocalRootProject / baseDirectory).value / "target" / "scalachess"

  publishLocal.dependsOn(scalachess / Compile / publishM2).value
  IO.delete(depDir)
}

publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
publishMavenStyle := true
publishTo := Some(Resolver.file("file", file(sys.props("user.home")) / ".m2" / "repository"))

publish := {
  publishScalalibDependency.value
  publishScalachessDependency.value
  publish.value
}

lazy val scalalib = ProjectRef(uri("git://github.com/lichess-org/scalalib.git#2d3b578"), "core")
lazy val scalachess = ProjectRef(uri("git://github.com/lichess-org/scalachess.git#dfbb10c"), "scalachess")
lazy val scalachessWrapper = project.in(file(".")).withId("scalachess-wrapper").dependsOn(scalalib).dependsOn(scalachess)