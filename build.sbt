import BuildHelper._

resolvers += Resolver.sonatypeRepo("snapshots")

inThisBuild(
  List(
    organization := "dev.zio",
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scmInfo      := Some(
      ScmInfo(url("https://github.com/zio/zio-insight/"), "scm:git:git@github.com:zio/zio-insight.git")
    )
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val root =
  (project in file("."))
    .aggregate(insightsJs, docs)
    .settings(
      publish / skip := true
    )
    .enablePlugins(BuildInfoPlugin)

lazy val insights =
  crossProject(JSPlatform)
    .in(file("zio-insights"))
    .settings(
      crossScalaVersions := Seq(Version.Scala213, Version.ScalaDotty),
      stdSettings("zio.insights"),
      libraryDependencies ++= Seq(
        "dev.zio" %%% "zio" % Version.zio
      )
    )
    .jsSettings(
      crossScalaVersions              := Seq(Version.Scala213),
      libraryDependencies ++= Seq(
        "dev.zio"           %%% "zio"                       % Version.zio,
        "dev.zio"           %%% "zio-json"                  % Version.zioJson,
        "com.raquo"         %%% "laminar"                   % Version.laminar,
        "io.laminext"       %%% "websocket"                 % Version.laminext,
        "org.scala-js"      %%% "scalajs-java-securerandom" % Version.scalaJsSecureRandom,
        "io.github.cquiroz" %%% "scala-java-time"           % Version.scalaJavaTime
      ),
      scalaJSLinkerConfig ~= {
        _.withModuleKind(ModuleKind.ESModule)
      },
      scalaJSLinkerConfig ~= {
        _.withSourceMap(true)
      },
      scalaJSUseMainModuleInitializer := true
    )
    .settings(buildInfoSettings("zio.insights"))
    .enablePlugins(BuildInfoPlugin)

lazy val insightsJs = insights.js

lazy val docs = project
  .in(file("zio-insight-docs"))
  .settings(
    publish / skip    := true,
    moduleName        := "zio-insight-docs",
    libraryDependencies ++= Seq("dev.zio" %% "zio" % Version.zio),
    projectName       := "ZIO Insight",
    badgeInfo         := Some(
      BadgeInfo(
        artifact = "zio-insight_2.12",
        projectStage = ProjectStage.Development
      )
    ),
    docsPublishBranch := "main"
  )
  .enablePlugins(WebsitePlugin)
