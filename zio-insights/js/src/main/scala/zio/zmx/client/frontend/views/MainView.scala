package zio.insight.frontend.views

import com.raquo.laminar.api.L._

import zio.insight.frontend.components._
import zio.insight.frontend.state._
import zio.insight.frontend.utils.Modifiers._

object MainView {

  private val sigDashboard = AppState.dashBoard.signal
  private val themeSignal  = AppState.theme.signal

  def render: Div =
    div(
      dataTheme(themeSignal),
      cls := "p-3 w-screen h-screen flex flex-col bg-accent overflow-hidden",
      NavBar.render,
      DashboardView.render(sigDashboard)
    )
}
