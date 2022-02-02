package zio.insight.webapp

import org.scalajs.dom

import zio._

object Insight:

  private lazy val createElement = ZIO
    .fromOption(Option(dom.document.getElementById("root")))
    .catchAll { _ =>
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      UIO(elem)
    }

  private lazy val program = for {
    _ <- ZIO.logInfo(s"Hello insight")
    // container <- createElement
    // _         <- emptyContainer
    // _         <- renderAppInContainer.provide(container)
    // _         <- ZIO.effectTotal {
    //                val link = dom.document.createElement("link").asInstanceOf[dom.html.Link]
    //                link.`type` = "image/x-icon"
    //                link.rel = "shortcut icon"
    //                link.href = Icon.name
    //                dom.document.head.appendChild(link)
    //              }
  } yield ()

  private def theMain(): Unit =
    zio.Runtime.default.unsafeRun(program)

  def main(args: Array[String]): Unit = println("Hello Andreas")

end Insight
