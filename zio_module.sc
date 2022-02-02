// format: off
import $ivy.`com.goyeau::mill-scalafix::0.2.8`
import com.goyeau.mill.scalafix.ScalafixModule

import $file.cross
import cross.Cross._
// format: on

import mill._
import mill.define.Sources
import mill.define.Target
import mill.modules.Jvm
import mill.scalajslib.ScalaJSModule
import mill.scalalib._
// Scalafix and Scala Format
import mill.scalalib.scalafmt.ScalafmtModule
import os.Path

trait ZIOModule extends SbtModule with ScalafmtModule with ScalafixModule with PublishModule { outer =>
  def projectRoot: Path
  def deps: Deps

  def scalafixScalaBinaryVersion = T {
    deps.prjScalaVersion match {
      case PrjScalaVersion.Scala_2_13  => "2.13"
      case PrjScalaVersion.Scala_3_1_0 => "3"
    }
  }

  def moduleName = millModuleSegments.parts.filterNot(_.equals(deps.prjScalaVersion.version)).mkString("-")

  override def millSourcePath = projectRoot / moduleName

  override def artifactName: T[String] = T(moduleName)

  def extraSources: Seq[String] = deps.prjScalaVersion match {
    case PrjScalaVersion.Scala_2_13  => Seq("2.13")
    case PrjScalaVersion.Scala_3_1_0 => Seq("3.x")
  }

  def createSourcePaths(prjKind: PrjKind, scope: String, extra: Seq[String]): Seq[PathRef] =
    (Seq("scala") ++ extra.map(e => s"scala-$e")).flatMap(ep =>
      Seq(
        PathRef(millSourcePath / prjKind.kind / "src" / scope / ep),
        PathRef(millSourcePath / "shared" / "src" / scope / ep)
      )
    )

  override def sources: Sources = T.sources(createSourcePaths(PrjKind.JvmProject, "main", extraSources))

  private val silencerVersion  = "1.7.7"
  private lazy val silencerLib = s"com.github.ghik:::silencer-lib:$silencerVersion"

  override def scalacOptions = T {

    val fatalWarnings: Seq[String] =
      if (sys.env.contains("CI")) {
        Seq("-Werror")
      } else {
        Seq.empty
      }

    val stdOptions: Seq[String] = Seq(
      "-deprecation",
      "-encoding",
      "UTF-8"
    ) ++ fatalWarnings

    deps.prjScalaVersion match {
      case PrjScalaVersion.Scala_2_13  =>
        stdOptions ++
          Seq(
            "-feature",
            "-language:higherKinds",
            "-language:existentials",
            "-unchecked",
            "-Ywarn-unused",
            "-Wunused:imports",
            "-Wunused:patvars",
            "-Wunused:privates",
            "-Wvalue-discard",
            "-Xsource:3"
          )
      case PrjScalaVersion.Scala_3_1_0 =>
        stdOptions
    }
  }

  override def scalacPluginIvyDeps =
    T {
      val libs: Agg[Dep] = deps.prjScalaVersion match {
        case PrjScalaVersion.Scala_2_13 => Agg(ivy"$silencerLib")
        case _                          => Agg.empty[Dep]
      }
      super.scalacPluginIvyDeps() ++ libs
    }

  override def ivyDeps =
    T {
      val libs: Agg[Dep] = deps.prjScalaVersion match {
        case PrjScalaVersion.Scala_2_13 => Agg(ivy"$silencerLib")
        case _                          => Agg.empty[Dep]
      }
      super.ivyDeps() ++ libs ++ Agg(deps.zio)
    }

  trait Tests extends super.Tests with ScalafmtModule with ScalafixModule {
    override def scalaVersion               = outer.scalaVersion
    override def scalafixScalaBinaryVersion = outer.scalafixScalaBinaryVersion

    override def artifactName = T(outer.artifactName() + "-test")

    override def testFramework: T[String] = T("zio.test.sbt.ZTestFramework")

    override def ivyDeps = T(outer.ivyDeps() ++ Agg(deps.zioTest, deps.zioTestSbt))

    override def sources: Sources = T.sources(outer.createSourcePaths(PrjKind.JvmProject, "test", outer.extraSources))
  }

  trait JSModule extends SbtModule with ScalafmtModule with ScalafixModule with ScalaJSModule { outerJS =>

    override def scalaVersion: T[String] = outer.scalaVersion

    override def scalaJSVersion: T[String] = T(outer.deps.scalaJSVersion)

    override def artifactName = T(outer.moduleName + "-js")

    override def sources: Sources = T.sources(outer.createSourcePaths(PrjKind.JsProject, "main", outer.extraSources))

    override def ivyDeps = outer.ivyDeps

    trait Tests extends outer.Tests with ScalaJSModule {
      override def scalaJSVersion: Target[String] = outerJS.scalaJSVersion

      override def artifactName = T(outerJS.artifactName() + "-test")

      override def testFramework: T[String] = T("zio.test.sbt.ZTestFramework")

      override def sources: Sources = T.sources(outer.createSourcePaths(PrjKind.JsProject, "test", outer.extraSources))

      override def ivyDeps = T(outerJS.ivyDeps() ++ Agg(deps.zioTest, deps.zioTestSbt))
    }

  }
} /* end ZIOModule */
