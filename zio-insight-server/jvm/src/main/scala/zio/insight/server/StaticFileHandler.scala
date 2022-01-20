package zio.insight.server

import java.nio.file.{Files, Path => NioPath, Paths}

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import zio._

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufOutputStream
import zhttp.http._

object StaticFileHandler:

  private lazy val defaultContentType = "application/octet-stream"

  private lazy val extensions = Chunk(
    (".htm", "text/html"),
    (".html", "text/html"),
    (".txt", "text/text"),
    (".json", "application/json")
  )

  def handle(baseDir: String): HttpApp[Any, Nothing] =
    Http.collect[Request] { case Method.GET -> !! / relPath =>
      val relFile = if (relPath.endsWith("/")) relPath + "index" else if relPath.isEmpty then "index" else relPath
      candidates(baseDir, relFile)
        .find(probe)
        .map(createResponse)
        .getOrElse(Response.fromHttpError(HttpError.NotFound(Path(relPath))))
    }

  private def createResponse(p: NioPath): Response = Try {
    HttpData.fromChunk(Chunk.fromArray(Files.readAllBytes(p)))
  } match {
    case Success(d) =>
      Response(
        headers = Headers.contentType(contentType(p)),
        data = d
      )
    case Failure(t) =>
      Response.fromHttpError(HttpError.InternalServerError(t.getMessage, None))
  }

  private def contentType(p: NioPath) =
    extensions.find(e => p.toFile.getAbsolutePath.endsWith(e._1)).map(_._2).getOrElse(defaultContentType)

  private val hasDefaultExtension: String => Boolean = relPath => extensions.exists(e => relPath.endsWith(e._1))

  private def candidates(baseDir: String, relPath: String) =
    if hasDefaultExtension(relPath) then Chunk(Paths.get(baseDir, relPath))
    else (Chunk(("", "")) ++ extensions).map(e => Paths.get(baseDir, relPath + e._1))

  private def probe(p: NioPath) =
    Try {
      val f = p.toFile
      f.exists && f.isFile && f.canRead
    }.getOrElse(false)

end StaticFileHandler
