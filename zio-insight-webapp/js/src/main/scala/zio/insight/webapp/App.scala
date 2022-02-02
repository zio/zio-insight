package zio.insight.webapp

import com.raquo.laminar.api.L._

import org.scalajs.dom.html

import zio.insight.ui.components.Component

final class App private () extends Component[html.Div]:

  override val element = div(
    p("Hello Andreas")
  )

end App
