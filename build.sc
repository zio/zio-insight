// format: off
import $file.npm_run
import npm_run.NpmRunModule

import $file.build_utils
import build_utils.BuildUtils
// Add simple docusaurus2 support for mill
import $ivy.`de.wayofquality.blended::de.wayofquality.blended.mill.docusaurus2::0.0.3`
import de.wayofquality.mill.docusaurus2.Docusaurus2Module
// Add simple mdoc support for mill
import $ivy.`de.wayofquality.blended::de.wayofquality.blended.mill.mdoc::0.0.4`
import de.wayofquality.mill.mdoc.MDocModule

import $file.cross
import cross.Cross._

import $file.zio_module
import zio_module.ZIOModule

import $file.tailwind_module
import tailwind_module.TailwindModule
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

object zio extends Module {

  val projectDir = build.millSourcePath

  object site extends Docusaurus2Module with MDocModule {
    override def scalaVersion      = T(PrjScalaVersion.default.version)
    override def mdocSources       = T.sources(projectDir / "docs")
    override def docusaurusSources = T.sources(projectDir / "website")

    override def watchedMDocsDestination: T[Option[Path]] = T(Some(docusaurusBuild().path / "docs"))

    override def compiledMdocs: Sources = T.sources(mdoc().path)
  }

  object insight                              extends Cross[ZIOInsight](PrjScalaVersion.default.version)
  class ZIOInsight(crossScalaVersion: String) extends Module {
    val prjScalaVersion = PrjScalaVersion(crossScalaVersion)
    val prjDeps         = prjScalaVersion match {
      case PrjScalaVersion.Scala_2_13  => Deps_213
      case PrjScalaVersion.Scala_3_1_0 => Deps_31
    }

    object server extends ZIOModule {
      override val projectRoot = projectDir
      override val deps        = prjDeps

      override def scalaVersion = T(crossScalaVersion)

      override def ivyDeps = T(super.ivyDeps() ++ Agg(deps.zioHttp))

      def start() = T.command {

        val baseDir = webapp.js.pkgServer().path.toIO.getAbsolutePath

        T.log.info(s"Starting Insight Server with base directory <$baseDir>")

        Jvm.runSubprocess(
          "zio.insight.server.InsightServer",
          runClasspath().map(_.path),
          jvmArgs = Seq(s"-DbaseDir=$baseDir", "-Djava.net.preferIPv4Stack=true")
        )
      }

      object test extends super.Tests()
    }

    object ui extends Module {

      object components extends ZIOModule {
        override val projectRoot = projectDir
        override val deps        = prjDeps

        override def scalaVersion = T(crossScalaVersion)

        object js extends super.JSModule {
          override def ivyDeps = T {
            super.ivyDeps() ++ Agg(
              deps.scalaJsDom,
              deps.laminar,
              deps.airstream
            )
          }
        }
      }

    }

    object webapp extends ZIOModule {
      override val projectRoot = projectDir
      override val deps        = prjDeps

      override def scalaVersion = T(crossScalaVersion)

      object js extends super.JSModule with TailwindModule {
        override val projectRoot = projectDir

        override def ivyDeps = T(
          super.ivyDeps() ++ Agg(
            deps.scalaJsDom,
            deps.laminar,
            deps.airstream
          )
        )

        override def moduleDeps = super.moduleDeps ++ Seq(zio.insight(crossScalaVersion).ui.components.js)

        object test extends super.Tests {}

      }
    }

    object core extends ZIOModule {
      override val projectRoot = projectDir
      override val deps        = prjDeps

      override def scalaVersion = T(crossScalaVersion)

      object test extends super.Tests {}

      object js extends super.JSModule {

        override def ivyDeps = T(super.ivyDeps() ++ Agg(deps.scalaJsDom))
        object test extends super.Tests {}
      }
    }
  }
}
