package zio.insight.frontend

import com.raquo.laminar.api.L._

import org.scalajs.dom

import zio.insight.frontend.views._

object Main {

  def main(args: Array[String]): Unit = {
    val _ = documentEvents.onDomContentLoaded.foreach { _ =>
      val appContainer = dom.document.querySelector("#app")
      appContainer.innerHTML = ""
      val _            = render(appContainer, MainView.render)
    }(unsafeWindowOwner)
  }

}
