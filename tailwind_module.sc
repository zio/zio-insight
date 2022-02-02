// format: off
import $file.build_utils
import build_utils.BuildUtils
import $file.npm_run
import npm_run.NpmRunModule
// format: on
import mill._
import mill.scalajslib.ScalaJSModule
import mill.scalalib._
import os.Path

trait TailwindModule extends ScalaJSModule with NpmRunModule {

  def projectRoot: Path

  private lazy val isProd =
    sys.env.getOrElse("PROD", "false").equalsIgnoreCase("true")

  def tailwindSources = T.sources(millSourcePath / "tailwind")

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
}
