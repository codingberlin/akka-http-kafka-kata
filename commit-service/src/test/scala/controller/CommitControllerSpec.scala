package controller

import org.scalatest.{FlatSpec, _}
import org.scalatest.mockito.MockitoSugar
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import model.api._
import model.kafka.Successful
import service.CommitService
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.libs.json.Json
import Protocol._

import scala.concurrent.Future

class CommitControllerSpec
    extends FlatSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest {

  "CommitController" should "response with NoContent when CommitService succeeds" in {
    val (commitController, commitService) = commitControllerAndMocks
    when(commitService.commit(any()))
      .thenReturn(Future.successful(Successful(true)))

    Post("/commit", Json.toJson(anyBooking).toString) ~> commitController.route ~> check {
      status shouldEqual StatusCodes.NoContent
    }

    verify(commitService, times(1)).commit(anyBooking)
  }

  it should "response with InternalServerError when CommitService does not succeed" in {
    val (commitController, commitService) = commitControllerAndMocks
    when(commitService.commit(any()))
      .thenReturn(Future.successful(Successful(false)))

    Post("/commit", Json.toJson(anyBooking).toString) ~> commitController.route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }

    verify(commitService, times(1)).commit(anyBooking)
  }

  it should "response with InternalServerError when Future of CommitService fails" in {
    val (commitController, commitService) = commitControllerAndMocks
    when(commitService.commit(any()))
      .thenReturn(Future.successful(Successful(false)))

    Post("/commit", Json.toJson(anyBooking).toString) ~> commitController.route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }

    verify(commitService, times(1)).commit(anyBooking)
  }

  it should "response with BadRequest and error messages when json has wrong schema" in {
    val (commitController, commitService) = commitControllerAndMocks
    when(commitService.commit(any()))
      .thenReturn(Future.successful(Successful(false)))

    Post("/commit", "{}") ~> commitController.route ~> check {
      status shouldEqual StatusCodes.BadRequest
      entityAs[String] shouldEqual Json
        .toJson(Seq(
          ValidationErrors(Path("/flightnumber"),
                           Seq(ValidationError("error.path.missing"))),
          ValidationErrors(Path("/userId"),
                           Seq(ValidationError("error.path.missing"))),
          ValidationErrors(Path("/provider"),
                           Seq(ValidationError("error.path.missing"))),
          ValidationErrors(Path("/persons"),
                           Seq(ValidationError("error.path.missing"))),
          ValidationErrors(Path("/price"),
                           Seq(ValidationError("error.path.missing")))
        ))
        .toString
    }

    verify(commitService, times(0)).commit(any())
  }

  it should "response with BadRequest when empty body is given" in {
    val (commitController, commitService) = commitControllerAndMocks
    when(commitService.commit(any()))
      .thenReturn(Future.successful(Successful(false)))

    Post("/commit", "") ~> commitController.route ~> check {
      status shouldEqual StatusCodes.BadRequest
      entityAs[String] shouldEqual Json
        .toJson(
          Seq(
            ValidationErrors(Path("/"),
                             Seq(ValidationError("payload must not be empty")))
          ))
        .toString
    }

    verify(commitService, times(0)).commit(any())
  }

  private def commitControllerAndMocks = {
    val commitService = mock[CommitService]
    val commitController = new CommitController(commitService)

    (commitController, commitService)
  }

  val anyBooking = Booking(
    UserId("123abc"),
    Seq(
      Person(
        Firstname("Max"),
        Lastname("Mustermann"),
        Seat("A5")
      )),
    Price(69.99),
    Flightnumber("EZY8124"),
    Provider("easyjet")
  )
}
