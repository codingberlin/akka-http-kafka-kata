package controller

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import model.api._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, _}
import persistence.BookingsRepository
import play.api.libs.json.{JsSuccess, Json}

import scala.concurrent.Future
import model.api.Protocol._

class BookingsControllerSpec
    extends FlatSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest {

  "BookingsController" should "response with bookings when BookingsRepository succeeds" in {
    val (bookingsController, bookingsRepository) = bookingsControllerAndMocks
    when(bookingsRepository.findByUserId(UserId(any())))
      .thenReturn(Future.successful(Set(anyBooking)))

    Get("/bookings?userid=1701") ~> bookingsController.route ~> check {
      status shouldEqual StatusCodes.OK
      Json.parse(entityAs[String]).validate[Seq[Booking]] shouldEqual JsSuccess(
        Seq(anyBooking))

    }

    verify(bookingsRepository, times(1)).findByUserId(UserId("1701"))
  }

  it should "response with InternalServerError when BookingsRepository does not succeed" in {
    val (bookingsController, bookingsRepository) = bookingsControllerAndMocks
    when(bookingsRepository.findByUserId(UserId(any())))
      .thenReturn(Future.failed(new Exception("Expected Test Exception")))

    Get("/bookings?userid=1701") ~> bookingsController.route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }

    verify(bookingsRepository, times(1)).findByUserId(UserId("1701"))
  }

  private def bookingsControllerAndMocks = {
    val bookingsRepository = mock[BookingsRepository]
    val bookingsController = new BookingsController(bookingsRepository)

    (bookingsController, bookingsRepository)
  }

  val anyBooking = Booking(
    bookingId = UUID.randomUUID(),
    UserId("1701"),
    NumberOfPersons(1),
    Flightnumber("EZY8124"),
  )
}
