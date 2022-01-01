import mill._
import mill.api.{Loose, Result}
import mill.define.Target
import mill.scalajslib.ScalaJSModule
import mill.scalalib._

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

sealed trait PrjKind { val kind: String }
object PrjKind {
  case object JvmProject extends PrjKind{ val kind = "jvm"}
  case object JsProject extends PrjKind{ val kind = "js"}
}

sealed trait PrjScalaVersion { val version : String }
object PrjScalaVersion {
  case object Scala_2_13_7 extends PrjScalaVersion{ val version = "2.13.7" }
  case object Scala_3_1_0 extends PrjScalaVersion{ val version = "3.1.0" }

  def apply(v : String) : PrjScalaVersion = v match {
    case Scala_2_13_7.version => Scala_2_13_7
    case Scala_3_1_0.version => Scala_3_1_0
    case v => throw new Exception(s"Unknown Scala version <$v>")
  }

  val default = Scala_3_1_0
  val all = Seq(Scala_2_13_7, Scala_3_1_0)
}

trait Deps {
  def prjScalaVersion : PrjScalaVersion
  val scalaVersion = prjScalaVersion.version
  val scalaJSVersion = "1.8.0"

  val zioVersion = "2.0.0-RC1"

  val zio = ivy"dev.zio::zio::$zioVersion"
  val zioTest = ivy"dev.zio::zio-test::$zioVersion"
  val zioTestSbt = ivy"dev.zio::zio-test-sbt::$zioVersion"
}

object Deps_213 extends Deps { 
  override def prjScalaVersion : PrjScalaVersion = PrjScalaVersion.Scala_2_13_7
}

object Deps_31 extends Deps { 
  override def prjScalaVersion : PrjScalaVersion = PrjScalaVersion.Scala_3_1_0
}

trait ZIOModule extends SbtModule with ScalafmtModule with ScalafixModule { outer =>
  def deps : Deps

  def scalafixScalaBinaryVersion = T{
    deps.prjScalaVersion match {
      case PrjScalaVersion.Scala_2_13_7 => "2.13"
      case PrjScalaVersion.Scala_3_1_0 => "3"
    }
  }

  def moduleName = millModuleSegments.parts.filterNot(_.equals(deps.prjScalaVersion.version)).mkString("-")

  def extraSources: Seq[String] = deps.prjScalaVersion match {
    case PrjScalaVersion.Scala_2_13_7 => Seq("2.13")
    case PrjScalaVersion.Scala_3_1_0 => Seq("3.x")
  }

  def createSourcePaths(prjKind: PrjKind, scope: String, extra: Seq[String])  : Seq[PathRef] =
    (Seq("scala") ++ extra.map(e => s"scala-$e")).flatMap(ep => Seq(
      PathRef(millSourcePath / prjKind.kind / "src" / scope / ep),
      PathRef(millSourcePath / "shared" / "src" / scope / ep)
    ))

  override def sources: Sources = T.sources(createSourcePaths(PrjKind.JvmProject, "main", extraSources))

  private val silencerVersion = "1.7.7"
  private lazy val silencerLib = s"com.github.ghik:::silencer-lib:$silencerVersion"

  override def scalacOptions = T(
    deps.prjScalaVersion match {
      case PrjScalaVersion.Scala_2_13_7 => Seq(
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
      )
      case PrjScalaVersion.Scala_3_1_0 => Seq.empty[String]
    }
  )

  override def scalacPluginIvyDeps =
    T {
      val libs : Agg[Dep] = deps.prjScalaVersion match {
        case PrjScalaVersion.Scala_2_13_7 => Agg(ivy"$silencerLib")
        case _ => Agg.empty[Dep]
      }
      super.scalacPluginIvyDeps() ++ libs
    }

  override def ivyDeps =
    T {
      val libs: Agg[Dep] = deps.prjScalaVersion match {
        case PrjScalaVersion.Scala_2_13_7 => Agg(ivy"$silencerLib")
        case _ => Agg.empty[Dep]
      }
      super.ivyDeps() ++ libs
    }

  trait Tests extends super.Tests with ScalafmtModule with ScalafixModule  {
    override def scalaVersion = outer.scalaVersion
    override def scalafixScalaBinaryVersion = outer.scalafixScalaBinaryVersion

    override def testFramework: T[String] = T("zio.test.sbt.ZTestFramework")

    override def ivyDeps = T{ outer.ivyDeps() ++ Agg(deps.zioTest, deps.zioTestSbt)}

    override def sources: Sources = T.sources(outer.createSourcePaths(PrjKind.JvmProject, "test", outer.extraSources))
  }

  trait JSModule extends SbtModule with ScalafmtModule with ScalafixModule with ScalaJSModule { outerJS =>

    override def scalaVersion: T[String] = outer.scalaVersion

    override def scalaJSVersion: T[String] = T(outer.deps.scalaJSVersion)

    override def sources: Sources = T.sources(outer.createSourcePaths(PrjKind.JsProject, "main", outer.extraSources))

    override def ivyDeps = outer.ivyDeps

    trait Tests extends outer.Tests with ScalaJSModule {
      override def scalaJSVersion: Target[String] = outerJS.scalaJSVersion

      override def testFramework: T[String] = T("zio.test.sbt.ZTestFramework")

      override def sources: Sources = T.sources(outer.createSourcePaths(PrjKind.JsProject, "test", outer.extraSources))

      override def ivyDeps = T{outerJS.ivyDeps() ++ Agg(deps.zioTest, deps.zioTestSbt)}
    }

  }
}

object zio extends Module {

  object site extends Docusaurus2Module with MDocModule {
    override def scalaVersion = T(PrjScalaVersion.default.version)
    override def mdocSources = T.sources{ projectDir / "docs" }
    override def docusaurusSources = T.sources(
      projectDir / "website",
    )

    override def watchedMDocsDestination: T[Option[Path]] = T(Some(docusaurusBuild().path / "docs"))

    override def compiledMdocs: Sources = T.sources(mdoc().path)
  }

  object insight extends Cross[ZIOInsight](PrjScalaVersion.all.map(_.version):_*)
  class ZIOInsight(crossScalaVersion : String) extends Module {
    val prjScalaVersion = PrjScalaVersion(crossScalaVersion)

    object core extends ZIOModule {
      override def deps = prjScalaVersion match {
        case PrjScalaVersion.Scala_2_13_7 => Deps_213
        case PrjScalaVersion.Scala_3_1_0 => Deps_31
      }

      override def scalaVersion = T{crossScalaVersion}

      override def ivyDeps = T { super.ivyDeps() ++ Agg(deps.zio) }

      override def millSourcePath = projectDir / moduleName

      override def artifactName: T[String] = T(moduleName)

      def runDummy = T {runMain("zio.insight.HelloWorld")}

      object test extends super.Tests {}

      object js extends super.JSModule {
        object test extends super.Tests{}
      }
    }
  }
}

