package controller

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.stream.Materializer
import model.api.{Booking, UserId}
import persistence.BookingsRepository
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import scala.util.Try
import model.api.Protocol._

class BookingsController(bookingsRepository: BookingsRepository)(
    implicit val ex: ExecutionContext,
    actorSystem: ActorSystem,
    materializer: Materializer) {

  val route: Route =
    get {
      path("bookings") {
        parameters('userid) { userId: String =>
          val bookingsFuture = bookingsRepository.findByUserId(UserId(userId))

          onComplete(bookingsFuture) { bookings: Try[Set[Booking]] =>
            if (bookings.isSuccess)
              complete(Json.toJson(bookings.get).toString)
            else
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    }

  def start(interface: String, port: Int): Future[Done] = {
    val bindingFuture = Http().bindAndHandle(
      RouteResult.route2HandlerFlow(route),
      interface,
      port)
    println(
      s"Server online at http://$interface:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind())
  }

}
