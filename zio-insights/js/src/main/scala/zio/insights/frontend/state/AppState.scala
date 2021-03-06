package zio.insights.frontend.state

import scala.scalajs.js.typedarray._

import com.raquo.laminar.api.L._

import zio.insights.KeyTypes
import zio.insights.frontend.components._
import zio.insights.frontend.model._
import zio.insights.frontend.model.Layout._
import zio.metrics.MetricKey
import zio.metrics.MetricKeyType

import io.laminext.websocket.WebSocket

object AppState {

  val wsConnection: Var[Option[WebSocket[ArrayBuffer, ArrayBuffer]]] = Var(None)

  // The theme that is currently used
  val theme: Var[Theme.DaisyTheme] = Var(Theme.DaisyTheme.Wireframe)

  // This reflects whether the app is currently connected, it is set by the
  // WS handler when it has established a connection
  val connected                     = wsConnection.signal.map(maybeWs => maybeWs.isDefined)
  val clientID: Var[Option[String]] = Var(None)

  // This reflects if the user has hit the connect button and we shall try to connect
  // to the configured url
  val shouldConnect: Var[Boolean] = Var(false)

  // The initial WS URL we want to consume events from
  val connectUrl: Var[String] = Var("ws://localhost:8080/ws")

  // The currently displayed diagrams (order is important)
  val dashBoard: Var[Dashboard[PanelConfig]] =
    Var(defaultDashboard)

  // We keep the configuration for the displayed lines in a variable outside the actual display config,
  // so that manipulating the TSConfig does not necessarily trigger an update for the entire dashboard
  val timeSeries: Var[Map[String, Map[TimeSeriesKey, TimeSeriesConfig]]] = Var(Map.empty)

  // Also we keep the recorded data of all displayed panel in the AppState, so that the data won´t
  // be lost on a dashboard re-render
  val recordedData: Var[Map[String, LineChartModel]] = Var(Map.empty)
  val updatedData: EventBus[String]                  = new EventBus[String]

  // The currently available metrics
  val availableMetrics: Var[Set[MetricKey.Untyped]] = Var(Set.empty)

  private def selectedKeys(selector: KeyTypes): Signal[Set[MetricKey.Untyped]] = {
    val hasType: MetricKey.Untyped => Option[MetricKey.Untyped] = k =>
      k.keyType match {
        case _: MetricKeyType.Counter if selector == KeyTypes.Counter     => Some(k)
        case _: MetricKeyType.Gauge if selector == KeyTypes.Gauge         => Some(k)
        case _: MetricKeyType.Histogram if selector == KeyTypes.Histogram => Some(k)
        case _: MetricKeyType.Summary if selector == KeyTypes.Summary     => Some(k)
        case _: MetricKeyType.Frequency if selector == KeyTypes.Frequency => Some(k)
        case _                                                            => None
      }
    availableMetrics.signal.changes.map(all => all.map(hasType).collect { case Some(k) => k }).toSignal(Set.empty)
  }

  // Just some convenience to get all the known metric keys
  val knownCounters: Signal[Set[MetricKey.Untyped]]   = selectedKeys(KeyTypes.Counter)
  val knownGauges: Signal[Set[MetricKey.Untyped]]     = selectedKeys(KeyTypes.Gauge)
  val knownHistograms: Signal[Set[MetricKey.Untyped]] = selectedKeys(KeyTypes.Histogram)
  val knownSummaries: Signal[Set[MetricKey.Untyped]]  = selectedKeys(KeyTypes.Summary)
  val knownSetCounts: Signal[Set[MetricKey.Untyped]]  = selectedKeys(KeyTypes.Frequency)

  // Reset everything - is usually called upon disconnect
  def resetState(): Unit = {
    clientID.set(None)
    shouldConnect.set(false)
    dashBoard.set(defaultDashboard)
    recordedData.set(Map.empty)
    timeSeries.set(Map.empty)
    availableMetrics.set(Set.empty)
  }

  lazy val defaultDashboard: Dashboard[PanelConfig] = {

    val panel: String => Dashboard[PanelConfig] = s => Dashboard.Cell(PanelConfig.EmptyConfig.create(s))

    panel("ZMX Dashboard")
  }
}
