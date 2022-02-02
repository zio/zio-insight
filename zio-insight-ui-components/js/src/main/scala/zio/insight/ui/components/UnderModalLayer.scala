package zio.insight.ui.components

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement

import org.scalajs.dom.html

/**
 * This is a thin layer on top of the "normal" flow elements.
 *
 * Usage:
 *   - the `UnderModalLayer.element` is automatically added to the Laminar dom tree in the [[frontend.components.App]]
 *     component
 *   - when you want to show the [[UnderModalLayer]] element, you can simply insert [[scala.Unit]] inside the
 *     `UnderModalLayer.showWriter` observer
 *   - when the [[UnderModalLayer]] element is clicked on, it is automatically closed, and the
 *     `UnderModalLayer.closeModalEvents` stream is fed with [[scala.Unit]]. You can therefore do stuff with it if you
 *     need to.
 *   - the [[UnderModalLayer]] element has `zIndex` set to 5, so you need to put your own stuff on top of that.
 *   - if you need to close the modal yourself, you can emit in the `UnderModalLayer.closeModalWriter`
 *
 * @example
 * {{{
 *   div(
 *     button("click", onClick.mapTo(()) --> UnderModalLayer.showModalWriter, onClick --> [show your own stuff]),
 *     onMountCallback(
 *       ctx => UnderModalLayer.closeModalEvents.foreach(_ => println("Modal has been closed!"))(ctx.owner)
 *     )
 *   )
 * }}}
 *
 * Clicking on this button will show the modal, together with your own stuff (which should have `zIndex > 5`) and when
 * it is closed it will print
 */
final class UnderModalLayer private (
  showStream: EventStream[Boolean],
  closedObserver: Observer[Unit]
) extends Component[html.Div]:

  val visibleBus: EventBus[Boolean] = new EventBus

  val visible: Signal[Boolean] = EventStream
    .merge(
      visibleBus.events,
      showStream
    )
    .startWith(false)

  val element: ReactiveHtmlElement[html.Div] = div(
    position  := "absolute",
    left      := "0",
    top       := "0",
    width     := "100%",
    height    := "100%",
    zIndex    := 5,
    display <-- visible.map(if (_) "block" else "none"),
    onClick.mapTo(false) --> visibleBus,
    onClick.mapTo(()) --> closedObserver,
    className := "under-modal-layer"
  )
end UnderModalLayer

object UnderModalLayer:

  private val showBus                  = new EventBus[Boolean]
  val showModalWriter: Observer[Unit]  = showBus.writer.contramap[Unit](_ => true)
  val closeModalWriter: Observer[Unit] = showBus.writer.contramap(_ => false)

  private val closedBus                   = new EventBus[Unit]
  val closeModalEvents: EventStream[Unit] = closedBus.events

  val element = new UnderModalLayer(showBus.events, closedBus.writer)

end UnderModalLayer
