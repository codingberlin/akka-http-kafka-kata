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

    Post("/commit", anyBokkingJson) ~> commitController.route ~> check {
      status shouldEqual StatusCodes.NoContent
    }

    verify(commitService, times(1)).commit(anyBooking)
  }

  it should "response with InternalServerError when CommitService does not succeed" in {
    val (commitController, commitService) = commitControllerAndMocks
    when(commitService.commit(any()))
      .thenReturn(Future.successful(Successful(false)))

    Post("/commit", anyBokkingJson) ~> commitController.route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }

    verify(commitService, times(1)).commit(anyBooking)
  }

  it should "response with InternalServerError when Future of CommitService fails" in {
    val (commitController, commitService) = commitControllerAndMocks
    when(commitService.commit(any()))
      .thenReturn(Future.successful(Successful(false)))

    Post("/commit", anyBokkingJson) ~> commitController.route ~> check {
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
      entityAs[String] shouldEqual """[{"path":"/flightnumber","errors":["error.path.missing"]},{"path":"/userId","errors":["error.path.missing"]},{"path":"/provider","errors":["error.path.missing"]},{"path":"/persons","errors":["error.path.missing"]},{"path":"/price","errors":["error.path.missing"]}]"""
    }

    verify(commitService, times(0)).commit(any())
  }

  it should "response with BadRequest when empty body is given" in {
    val (commitController, commitService) = commitControllerAndMocks
    when(commitService.commit(any()))
      .thenReturn(Future.successful(Successful(false)))

    Post("/commit", "") ~> commitController.route ~> check {
      status shouldEqual StatusCodes.BadRequest
      entityAs[String] shouldEqual """[{"path":"/","errors":["payload must not be empty"]}]"""
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

  val anyBokkingJson = """{
                      |"userId": "123abc",
                      |"persons": [{
                      |  "firstname": "Max",
                      |  "lastname": "Mustermann",
                      |  "seat": "A5"
                      |}],
                      |"price": 69.99,
                      |"flightnumber": "EZY8124",
                      |"provider": "easyjet"
                      |}""".stripMargin
}
