import mill._
import mill.modules.Jvm
import os.Path

trait NpmRunModule extends Module {

  val projectDir: Path

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
