import com.goyeau.mill.scalafix.ScalafixModule

import $ivy.`com.goyeau::mill-scalafix:0.2.6`
// Add simple docusaurus2 support for mill
import $ivy.`de.wayofquality.blended::de.wayofquality.blended.mill.docusaurus2::0.0.1`
// Add simple mdoc support for mill
import $ivy.`de.wayofquality.blended::de.wayofquality.blended.mill.mdoc::0.0.1-4-0ce9fb`
import de.wayofquality.mill.docusaurus2.Docusaurus2Module
import de.wayofquality.mill.mdoc.MDocModule
import mill._
import mill.define.Sources
import mill.define.Target
import mill.modules.Jvm
import mill.scalajslib.ScalaJSModule
import mill.scalalib._
// Scalafix and Scala Format
import mill.scalalib.scalafmt.ScalafmtModule
import os.Path

// It's convenient to keep the base project directory around
val projectDir = build.millSourcePath

lazy val isProd =
  sys.env.getOrElse("PROD", "false").equalsIgnoreCase("true")

sealed trait PrjKind { val kind: String }
object PrjKind       {
  case object JvmProject extends PrjKind { val kind = "jvm" }
  case object JsProject  extends PrjKind { val kind = "js"  }
}

sealed trait PrjScalaVersion { val version: String }
object PrjScalaVersion       {
  case object Scala_2_13_7 extends PrjScalaVersion { val version = "2.13.7" }
  case object Scala_3_1_0  extends PrjScalaVersion { val version = "3.1.0"  }

  def apply(v: String): PrjScalaVersion = v match {
    case Scala_2_13_7.version => Scala_2_13_7
    case Scala_3_1_0.version  => Scala_3_1_0
    case v                    => throw new Exception(s"Unknown Scala version <$v>")
  }

  val default = Scala_3_1_0
  val all     = Seq(Scala_2_13_7, Scala_3_1_0)
}

object BuildUtils {

  def copySources(from: Seq[Path], to: Path) =
    from.foreach { path =>
      os.walk(path).foreach { file =>
        val relPath    = file.relativeTo(path)
        val dest: Path = to / relPath

        os.copy
          .over(file, dest, followLinks = true, replaceExisting = true, copyAttributes = true, createFolders = true)
      }
    }

  def cloneDirectory(from: Path, to: Path) = {
    os.list(to).foreach(os.remove.all)
    os.list(from).foreach { p =>
      os.copy.into(
        p,
        to,
        followLinks = true,
        replaceExisting = true,
        copyAttributes = true,
        createFolders = true,
        mergeFolders = false
      )
    }
  }
}

/* ----------------------------------------------------------------------------------------------------------
 * Dependency management
 * ---------------------------------------------------------------------------------------------------------- */
trait Deps {
  def prjScalaVersion: PrjScalaVersion
  val scalaVersion = prjScalaVersion.version

  val laminarVersion = "0.14.2"
  val scalaJSVersion = "1.8.0"
  val zioVersion     = "2.0.0-RC1"

  val airstream = ivy"com.raquo::airstream::$laminarVersion"
  val laminar   = ivy"com.raquo::laminar::$laminarVersion"

  val scalaJsDom = ivy"org.scala-js::scalajs-dom::2.1.0"

  val zioHttp = ivy"io.d11::zhttp:2.0.0-RC1"

  val zio        = ivy"dev.zio::zio::$zioVersion"
  val zioTest    = ivy"dev.zio::zio-test::$zioVersion"
  val zioTestSbt = ivy"dev.zio::zio-test-sbt::$zioVersion"
}

object Deps_213 extends Deps {
  override def prjScalaVersion: PrjScalaVersion = PrjScalaVersion.Scala_2_13_7
}

object Deps_31 extends Deps {
  override def prjScalaVersion: PrjScalaVersion = PrjScalaVersion.Scala_3_1_0
}

trait ZIOModule extends SbtModule with ScalafmtModule with ScalafixModule { outer =>
  def deps: Deps

  def scalafixScalaBinaryVersion = T {
    deps.prjScalaVersion match {
      case PrjScalaVersion.Scala_2_13_7 => "2.13"
      case PrjScalaVersion.Scala_3_1_0  => "3"
    }
  }

  def moduleName = millModuleSegments.parts.filterNot(_.equals(deps.prjScalaVersion.version)).mkString("-")

  override def millSourcePath = projectDir / moduleName

  override def artifactName: T[String] = T(moduleName)

  def extraSources: Seq[String] = deps.prjScalaVersion match {
    case PrjScalaVersion.Scala_2_13_7 => Seq("2.13")
    case PrjScalaVersion.Scala_3_1_0  => Seq("3.x")
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
      case PrjScalaVersion.Scala_2_13_7 =>
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
      case PrjScalaVersion.Scala_3_1_0  =>
        stdOptions
    }
  }

  override def scalacPluginIvyDeps =
    T {
      val libs: Agg[Dep] = deps.prjScalaVersion match {
        case PrjScalaVersion.Scala_2_13_7 => Agg(ivy"$silencerLib")
        case _                            => Agg.empty[Dep]
      }
      super.scalacPluginIvyDeps() ++ libs
    }

  override def ivyDeps =
    T {
      val libs: Agg[Dep] = deps.prjScalaVersion match {
        case PrjScalaVersion.Scala_2_13_7 => Agg(ivy"$silencerLib")
        case _                            => Agg.empty[Dep]
      }
      super.ivyDeps() ++ libs ++ Agg(deps.zio)
    }

  trait Tests extends super.Tests with ScalafmtModule with ScalafixModule {
    override def scalaVersion               = outer.scalaVersion
    override def scalafixScalaBinaryVersion = outer.scalafixScalaBinaryVersion

    override def testFramework: T[String] = T("zio.test.sbt.ZTestFramework")

    override def ivyDeps = T(outer.ivyDeps() ++ Agg(deps.zioTest, deps.zioTestSbt))

    override def sources: Sources = T.sources(outer.createSourcePaths(PrjKind.JvmProject, "test", outer.extraSources))
  }

  trait JSModule extends SbtModule with ScalafmtModule with ScalafixModule with ScalaJSModule { outerJS =>

    override def scalaVersion: T[String] = outer.scalaVersion

    override def scalaJSVersion: T[String] = T(outer.deps.scalaJSVersion)

    override def sources: Sources = T.sources(outer.createSourcePaths(PrjKind.JsProject, "main", outer.extraSources))

    override def ivyDeps = outer.ivyDeps

    // This is required to make web components developed with Scala.JS work
    override def useECMAScript2015 = T(true)

    trait Tests extends outer.Tests with ScalaJSModule {
      override def scalaJSVersion: Target[String] = outerJS.scalaJSVersion

      override def testFramework: T[String] = T("zio.test.sbt.ZTestFramework")

      override def sources: Sources = T.sources(outer.createSourcePaths(PrjKind.JsProject, "test", outer.extraSources))

      override def ivyDeps = T(outerJS.ivyDeps() ++ Agg(deps.zioTest, deps.zioTestSbt))
    }

  }
} /* end ZIOModule */

trait NpmRunModule extends Module {

  def npmSources = T.sources(millSourcePath / "npm")

  protected def runAndWait(cmd: Seq[String], envArgs: Map[String, String], dir: Path) = {
    val proc = Jvm.spawnSubprocess(cmd, envArgs.updated("PROJECT_DIR", projectDir.toIO.getAbsolutePath), dir)
    proc.join()
  }

  def npmInstall = T {
    val dest = T.dest

    npmSources().foreach { pr =>
      val srcDir = pr.path
      os.walk(srcDir).foreach { p =>
        val relPath = p.relativeTo(srcDir)
        val target  = dest / relPath
        os.copy.over(p, target, followLinks = true, replaceExisting = true, copyAttributes = true, createFolders = true)
      }
    }

    runAndWait(Seq("yarn", "install", "--check-files"), Map.empty, dest)

    PathRef(dest)
  }
} /* End NpmRunModule */

object zio extends Module {

  object site extends Docusaurus2Module with MDocModule {
    override def scalaVersion      = T(PrjScalaVersion.default.version)
    override def mdocSources       = T.sources(projectDir / "docs")
    override def docusaurusSources = T.sources(
      projectDir / "website"
    )

    override def watchedMDocsDestination: T[Option[Path]] = T(Some(docusaurusBuild().path / "docs"))

    override def compiledMdocs: Sources = T.sources(mdoc().path)
  }

  object insight                              extends Cross[ZIOInsight](PrjScalaVersion.default.version)
  class ZIOInsight(crossScalaVersion: String) extends Module {
    val prjScalaVersion = PrjScalaVersion(crossScalaVersion)
    val prjDeps         = prjScalaVersion match {
      case PrjScalaVersion.Scala_2_13_7 => Deps_213
      case PrjScalaVersion.Scala_3_1_0  => Deps_31
    }

    object server extends ZIOModule {
      override val deps = prjDeps

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

    object webapp extends ZIOModule {
      override val deps = prjDeps

      override def scalaVersion = T(crossScalaVersion)

      object js extends super.JSModule with NpmRunModule {
        def tailwindSources = T.sources(millSourcePath / "tailwind")

        override def ivyDeps = T(
          super.ivyDeps() ++ Agg(
            deps.scalaJsDom,
            deps.laminar,
            deps.airstream
          )
        )

        def pkgServer = {
          val bundleJS =
            if (isProd) T.task {
              rollupJS().path
            }
            else
              T.task {
                fastOpt().path
              }

          T {
            val dir = T.dest

            BuildUtils.copySources(resources().map(_.path), dir)
            os.copy.over(bundleJS(), dir / "insight.js")

            os.copy.into(tailwind().path, dir)

            PathRef(dir)
          }
        }

        def rollupJS = T {
          def dir = T.dest

          val fileRef =
            s"""const file = '${fullOpt().path.toIO.getAbsolutePath}'
               |module.exports = file""".stripMargin

          BuildUtils.cloneDirectory(npmInstall().path, dir)
          os.write.over(dir / "jsfile.js", fileRef.getBytes())
          runAndWait(Seq("yarn", "install", "--force"), Map.empty, dir)
          runAndWait(Seq("npx", "rollup", "-c"), Map.empty, dir)

          PathRef(dir)
        }

        def tailwind = T {
          val dir = T.dest

          BuildUtils.cloneDirectory(npmInstall().path, dir)
          runAndWait(Seq("yarn", "install", "--force"), Map.empty, dir)
          BuildUtils.copySources(tailwindSources().map(_.path), dir)
          runAndWait(Seq("npx", "tailwindcss", "-i", "./input.css", "-o", "insight.css"), Map.empty, dir)

          PathRef(dir / "insight.css")
        }

        object test extends super.Tests {}

      }
    }

    object core extends ZIOModule {

      override val deps = prjDeps

      override def scalaVersion = T(crossScalaVersion)

      object test extends super.Tests {}

      object js extends super.JSModule {

        override def ivyDeps = T(super.ivyDeps() ++ Agg(deps.scalaJsDom))
        object test extends super.Tests {}
      }
    }
  }
}
