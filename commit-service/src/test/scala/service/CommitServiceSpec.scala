package service

import model.api._
import model.kafka.Successful
import org.scalatest._
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class CommitServiceSpec extends FlatSpec with Matchers with MockitoSugar {

  "CommitService" should "success when to book and to persist to kafka succseeds" in {
    val (commitService, bookingService, kafkaService) = commitServiceAndMocks
    when(bookingService.book(any())).thenReturn(Future.successful(()))
    when(kafkaService.produceMessage(any(), Successful(anyBoolean())))
      .thenReturn(Future.successful(()))

    Await.result(commitService.commit(anyBooking), 2.seconds) shouldEqual Successful(
      true)

    verify(bookingService, times(1)).book(anyBooking)
    verify(kafkaService, times(1))
      .produceMessage(anyBooking, Successful(true))
  }

  it should "fail when to book fails" in {
    val (commitService, bookingService, kafkaService) = commitServiceAndMocks
    when(bookingService.book(any()))
      .thenReturn(Future.failed(new Exception("Expected Test Exception")))
    when(kafkaService.produceMessage(any(), Successful(anyBoolean())))
      .thenReturn(Future.successful(()))

    Await.result(commitService.commit(anyBooking), 2.seconds) shouldEqual Successful(
      false)

    verify(bookingService, times(1)).book(anyBooking)
    verify(kafkaService, times(1))
      .produceMessage(anyBooking, Successful(false))
  }

  it should "fail when to book succseeds but persit to kafka fails" in {
    val (commitService, bookingService, kafkaService) = commitServiceAndMocks
    when(bookingService.book(any())).thenReturn(Future.successful(()))
    when(kafkaService.produceMessage(any(), Successful(anyBoolean())))
      .thenReturn(Future.failed(new Exception("Expected Test Exception")))

    Await.result(commitService.commit(anyBooking), 2.seconds) shouldEqual Successful(
      false)

    verify(bookingService, times(1)).book(anyBooking)
    verify(kafkaService, times(1))
      .produceMessage(anyBooking, Successful(true))
  }

  private def commitServiceAndMocks = {
    val bookingServiceMock = mock[BookingService]
    val kafkaServiceMock = mock[KafkaService]
    val commitService = new CommitService(bookingServiceMock, kafkaServiceMock)

    (commitService, bookingServiceMock, kafkaServiceMock)
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
