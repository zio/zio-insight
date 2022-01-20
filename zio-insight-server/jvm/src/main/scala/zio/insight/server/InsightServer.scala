package zio.insight.server

import java.io.File
import java.net.InetSocketAddress

import zio.{System => ZioSystem, _}

import zhttp.http._
import zhttp.service.Server

object InsightServer extends ZIOAppDefault:
  private val bindHost = "0.0.0.0"
  private val bindPort = 8888
  private val baseDir  = sys.props.getOrElse("baseDir", sys.props.getOrElse("user.dir", "."))

  private val app: HttpApp[Any, Nothing] = StaticFileHandler.handle(baseDir)

  override def run = Server.start(InetSocketAddress(bindHost, bindPort), app.silent)

end InsightServer
