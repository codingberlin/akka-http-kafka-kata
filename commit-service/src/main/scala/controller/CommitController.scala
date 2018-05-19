package controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Route, RouteResult, StandardRoute}
import model.api.{Booking, Path, ValidationError, ValidationErrors}
import model.kafka.Successful
import play.api.libs.json._
import service.CommitService
import akka.http.scaladsl.server.Directives._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import Protocol._
import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer

import scala.io.StdIn

class CommitController(commitService: CommitService)(
    implicit val ex: ExecutionContext,
    actorSystem: ActorSystem,
    materializer: Materializer) {

  val route: Route =
    post {
      path("commit") {
        entity(as[String]) { body =>
          val commitResult = Json.parse(body).validate[Booking] match {
            case JsSuccess(booking, _) =>
              commitBooking(booking)
            case JsError(errors) =>
              sendValidationErrors(errors)
          }

          onComplete(commitResult) { route: Try[StandardRoute] =>
            route.getOrElse(complete(StatusCodes.InternalServerError))
          }
        }
      }
    }

  private def commitBooking(booking: Booking) =
    commitService.commit(booking).map {
      case Successful(true) =>
        complete(StatusCodes.NoContent)
      case Successful(false) =>
        complete(StatusCodes.InternalServerError)
    }

  private def sendValidationErrors(
      errors: Seq[(JsPath, Seq[JsonValidationError])]) = {
    val errorMessages: Seq[ValidationErrors] = errors
      .map {
        case (path, errors) =>
          ValidationErrors(Path(path.toString),
                           errors.map(_.message).map(ValidationError))
      }
    Future.successful(
      complete(StatusCodes.BadRequest, Json.toJson(errorMessages).toString))
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
