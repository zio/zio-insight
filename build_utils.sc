import os.Path

object BuildUtils {

  def copySources(from: Seq[Path], to: Path): Unit =
    from.foreach { path =>
      os.walk(path).foreach { file =>
        val relPath    = file.relativeTo(path)
        val dest: Path = to / relPath

        os.copy
          .over(file, dest, followLinks = true, replaceExisting = true, copyAttributes = true, createFolders = true)
      }
    }

  def cloneDirectory(from: Path, to: Path): Unit = {
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
