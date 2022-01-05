package zio.insight.server

import java.io.File
import java.net.InetSocketAddress
import java.nio.file.Path
import java.nio.file.Paths

import zio.{System => ZioSystem, _}

import uzhttp._
import uzhttp.Request.Method
import uzhttp.server.Server

object InsightServer extends ZIOAppDefault:
  private val bindHost = "0.0.0.0"
  private val bindPort = 8888

  private val staticContext = "/static/"

  private val server = Server
    .builder(new InetSocketAddress(bindHost, bindPort))
    .handleSome(handleDummy)
    .handleSome(handleDirectory)
    .serve

  private lazy val handleDummy: PartialFunction[Request, ZIO[Any, HTTPError, Response]] =
    case req if req.uri.getPath.equals("/") => ZIO.succeed(Response.html("Hello Andreas"))

  private lazy val handleDirectory: PartialFunction[Request, ZIO[ZioSystem, HTTPError, Response]] =
    val baseDir = sys.props.getOrElse("baseDir", sys.props.getOrElse("user.dir", "."))
    new StaticFileHandler(staticContext, baseDir).handle

  override def run = for _ <- server.useForever.orDie
  yield ()

end InsightServer
