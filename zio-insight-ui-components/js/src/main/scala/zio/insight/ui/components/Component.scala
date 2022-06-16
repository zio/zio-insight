package zio.insight.ui.components

import scala.language.implicitConversions

import com.raquo.laminar.nodes.ReactiveHtmlElement

import org.scalajs.dom

trait Component[Ref <: dom.html.Element]:
  val element: ReactiveHtmlElement[Ref]
end Component

object Component:
  implicit def asElement[Ref <: dom.html.Element](component: Component[Ref]): ReactiveHtmlElement[Ref] =
    component.element
end Component
