package zio.insights.frontend.views

import com.raquo.laminar.api.L._

import zio.insights.frontend.utils.Implicits._
import zio.metrics.MetricKey

final case class MetricsSelector(label: String, observer: Observer[MetricKey.Untyped], style: String = "secondary") {
  def render($metrics: Signal[Set[MetricKey.Untyped]]): HtmlElement =
    div(
      cls := "flex flex-wrap",
      child <-- $metrics.map {
        case e if e.isEmpty => emptyNode
        case metrics        =>
          div(
            cls := s"card w-full bg-$style text-$style-content rounded bordered form-control",
            div(
              cls := "card-body",
              h2(
                cls := "card-title",
                s"$label:"
              ),
              div(
                cls := "flex flex-wrap",
                metrics.map { metricKey =>
                  div(
                    cls := "badge badge-info text-xl cursor-pointer p-4 m-3",
                    metricKey.longName,
                    onClick.map(_ => metricKey) --> observer
                  )
                }.toSeq
              )
            )
          )
      }
    )
}
