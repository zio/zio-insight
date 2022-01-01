import mill._
import mill.scalalib._
import mill.modules.Jvm

// Scalafix and Scala Format
import mill.scalalib.scalafmt.ScalafmtModule
import $ivy.`com.goyeau::mill-scalafix:0.2.6`
import com.goyeau.mill.scalafix.ScalafixModule

// Add simple mdoc support for mill
import $ivy.`de.wayofquality.blended::de.wayofquality.blended.mill.mdoc::0.0.1-4-0ce9fb`
import de.wayofquality.mill.mdoc.MDocModule

// Add simple docusaurus2 support for mill
import $ivy.`de.wayofquality.blended::de.wayofquality.blended.mill.docusaurus2::0.0.1`
import de.wayofquality.mill.docusaurus2.Docusaurus2Module

import mill.define.Sources
import os.Path

// It's convenient to keep the base project directory around
val projectDir = build.millSourcePath

trait Deps {
  def scalaVersion : String

  val zioVersion = "2.0.0-RC1"

  val zio = ivy"dev.zio::zio:$zioVersion"
  val zioTest = ivy"dev.zio::zio-test:$zioVersion"
  val zioTestSbt = ivy"dev.zio::zio-test-sbt:$zioVersion"
}

object Deps_213 extends Deps { 
  override def scalaVersion = "2.13.7"
}

object Deps_31 extends Deps { 
  override def scalaVersion = "3.1.0"
}

trait ZIOModule extends SbtModule with ScalafmtModule with ScalafixModule { outer =>
  def deps : Deps
  def scalafixScalaBinaryVersion = T("2.13")

  private val silencerVersion = "1.7.7"
  private lazy val silencerLib = s"com.github.ghik:::silencer-lib:$silencerVersion"

  override def scalacOptions = T(Seq(
    "-deprecation",
    "-Ywarn-unused",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:higherKinds",
    "-language:existentials",
    "-unchecked",
    "-Wunused:imports",
    "-Wvalue-discard",
    "-Wunused:patvars",
    "-Wunused:privates",
    "-Wvalue-discard"
  ))

  override def scalacPluginIvyDeps = { 
    T {
      val plugins : Agg[Dep] = if (scalaVersion().equals("2.13.7")) { 
        Agg(ivy"$silencerLib")
      } else { 
        Agg.empty[Dep]
      }
      super.scalacPluginIvyDeps() ++ plugins
    }
  }

  override def ivyDeps = {
    T {
      val libs : Agg[Dep] = if (scalaVersion().equals("2.13.7")) { 
        Agg(ivy"$silencerLib")
      } else { 
        Agg.empty[Dep]
      }
      super.ivyDeps() ++ libs
    }
  }

  trait Tests extends super.Tests with ScalafmtModule with ScalafixModule  {
    override def scalaVersion = outer.scalaVersion
    override def scalafixScalaBinaryVersion = outer.scalafixScalaBinaryVersion

    override def testFramework: T[String] = T("zio.test.sbt.ZTestFramework")

    override def ivyDeps = Agg(deps.zioTest, deps.zioTestSbt)
  }

}

object zio extends Cross[ZIOProject]("3.1.0")
class ZIOProject(crossScalaVersion: String) extends Module {

  object site extends Docusaurus2Module with MDocModule {
    override def scalaVersion = T(crossScalaVersion)
    override def mdocSources = T.sources{ projectDir / "docs" }
    override def docusaurusSources = T.sources(
      projectDir / "website",
    )

    override def watchedMDocsDestination: T[Option[Path]] = T(Some(docusaurusBuild().path / "docs"))

    override def compiledMdocs: Sources = T.sources(mdoc().path)
  }

  object insight extends Module { 
    object core extends ZIOModule {
      override def deps = crossScalaVersion match { 
        case "2.13.7" => Deps_213
        case _ => Deps_31
      }

      override def scalaVersion = T{crossScalaVersion}

      override def ivyDeps = T { super.ivyDeps() ++ Agg(deps.zio) }

      override def millSourcePath = projectDir / "zio-profiling" / "jvm"
      override def artifactName: T[String] = T{"zio-profiling"}

      object test extends super.Tests {}
    }
  }
}

