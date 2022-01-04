package zio.insight.server

import java.nio.file.Path
import java.nio.file.Paths

import scala.util.Try

import zio._

import uzhttp._
import uzhttp.Request.Method

class StaticFileHandler(context: String, baseDir: String):

  private lazy val defaultContentType = "application/octet-stream"

  private lazy val extensions = Chunk(
    (".htm", "text/html"),
    (".html", "text/html"),
    (".txt", "text/text"),
    (".json", "application/json")
  )

  def handle: PartialFunction[Request, ZIO[Any, HTTPError, Response]] =
    case req if req.uri.getPath.startsWith(context) && req.method.equals(Method.GET) =>
      for
        dir    <- ZIO.succeed(baseDir)
        relPath = req.uri.getPath.substring(context.length)
        relFile = if (relPath.endsWith("/")) relPath + "index" else if relPath.isEmpty then "index" else relPath
        file   <- ZIO.collectFirst(candidates(baseDir, relFile))(probe)
        resp   <- createResponse(req, file)
      yield resp

  private def createResponse(req: Request, p: Option[Path]) = p match
    case None    => ZIO.fail(HTTPError.NotFound(req.uri.getPath))
    case Some(p) =>
      Response.fromPath(p, req, contentType(p)).mapError(t => HTTPError.InternalServerError(t.getMessage, Some(t)))

  private def contentType(p: Path) =
    extensions.find(e => p.toFile.getAbsolutePath.endsWith(e._1)).map(_._2).getOrElse(defaultContentType)

  private val hasDefaultExtension: String => Boolean = relPath => extensions.exists(e => relPath.endsWith(e._1))

  private def candidates(baseDir: String, relPath: String) =
    if hasDefaultExtension(relPath) then Chunk(Paths.get(baseDir, relPath))
    else (Chunk(("", "")) ++ extensions).map(e => Paths.get(baseDir, relPath + e._1))

  private def probe(p: Path) =
    for
      file <- ZIO.succeed(p.toFile).tap(f => ZIO.logInfo(s"Probing for file <${f.getAbsolutePath}>"))
      res  <- ZIO.fromTry {
                Try {
                  val f = p.toFile
                  f.exists && f.isFile && f.canRead
                }
              }.catchAll(_ => ZIO.succeed(false))
    yield if res then Some(p) else None

end StaticFileHandler
