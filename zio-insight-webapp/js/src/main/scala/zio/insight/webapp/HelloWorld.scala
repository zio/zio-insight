package zio.insight.webapp

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

import org.scalajs.dom
import org.scalajs.dom.HTMLElement

@js.native
trait CustomElementsRegistry extends js.Any:
  def define(name: String, definition: Any): Unit = js.native
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

object HelloWorld:
  def main(args: Array[String]) = count

  private def count =
    0.to(10).foreach(i => println(s"Counting ... $i"))
end HelloWorld
