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

// A project specific module defining the target Scala versions and managing the
// Scala version specific compiler settings and dependencies
import $file.cross
import cross.Cross._

// General settings for all ZIOM Modules
import $file.zio_module
import zio_module.ZIOModule

// A module to build a webapp taking a ScalaJS mainline and tailwind definition sources
// to package the entire app into a deployable directory that could be served from any 
// Web Server
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

  // This is the checkout directory, will be passed to most modules for path calculations
  val projectDir = build.millSourcePath

  // Build the site with Docusaurus 2 and Mdoc
  object site extends Docusaurus2Module with MDocModule {
    override def scalaVersion      = T(PrjScalaVersion.default.version)
    // MD Sources that must be compiled with Scala MDoc
    override def mdocSources       = T.sources(projectDir / "docs")
    // MD Sources that are just plain MD files
    override def docusaurusSources = T.sources(projectDir / "website")

    override def watchedMDocsDestination: T[Option[Path]] = T(Some(docusaurusBuild().path / "docs"))
    override def compiledMdocs: Sources                   = T.sources(mdoc().path)
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

      // Just a convenient way to start the Insight server via mill
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

      // Reusable components
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

    // The Insight SPA
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
