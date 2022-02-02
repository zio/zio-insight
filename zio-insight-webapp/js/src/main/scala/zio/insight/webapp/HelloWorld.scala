package zio.insight.webapp

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.annotation.JSGlobal

import com.raquo.laminar.api.L._
import com.raquo.laminar.builders.HtmlTag
import com.raquo.laminar.nodes.ReactiveElement

import org.scalajs.dom

@js.native
trait CustomElementsRegistry extends js.Any:
  def define(name: String, definition: Any): Unit = js.native
end CustomElementsRegistry

object CustomElementsRegistry:
  def customElements =
    js.Dynamic.global.window.asInstanceOf[Window].customElements
end CustomElementsRegistry

@js.native
@JSGlobal
class Window extends dom.Window:
  val customElements: CustomElementsRegistry = js.native
end Window

@js.native
@JSGlobal
class HTMLTemplateElement extends HTMLElement:
  val content: HTMLElement = js.native
end HTMLTemplateElement

@js.native
@JSGlobal
class HTMLElement extends dom.HTMLEmbedElement:
  def attachShadow(options: js.Any): dom.HTMLEmbedElement = js.native
end HTMLElement

class ParagraphElement extends HTMLElement:
  self =>
  private lazy val template = dom.document.getElementById("my-paragraph").asInstanceOf[HTMLTemplateElement]
  private lazy val shadow   = self.attachShadow(literal(mode = "open"))

  val element = div(
    linkTag(href := "insight.css", rel := "stylesheet"),
    h1(cls       := "p-2 bg-yellow-200 rounded-full", "Test"),
    p(cls        := "p-2 bg-blue-100 rounded-full", "another Test")
  )

  shadow.innerHTML = ""
  shadow.appendChild(element.ref)
end ParagraphElement

object MainView:

  def render =
    div(
      1.to(10).map(_ => MyParagraph())
    )
end MainView

object MyParagraph:

  type El          = ReactiveElement[ParagraphElement]
  type ModFunction = MyParagraph.type => Mod[El]

  private val tag = new HtmlTag[ParagraphElement]("my-paragraph", void = false)

  def apply(mods: ModFunction*): El =
    tag(mods.map(_(MyParagraph)): _*)

end MyParagraph

// object HelloWorld:
//   def main(args: Array[String]) =

//     val _ = documentEvents.onDomContentLoaded.foreach { _ =>
//       CustomElementsRegistry.customElements.define("my-paragraph", js.constructorOf[ParagraphElement])
//       val appContainer = dom.document.getElementById("app")
//       appContainer.innerHTML = ""
//       val _            = render(appContainer, MainView.render)
//     }(unsafeWindowOwner)

// end HelloWorld
