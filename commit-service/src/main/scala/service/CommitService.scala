package service

import model.api.Booking
import model.kafka.Successful

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
  * Purpose of this service is to try to book the booking and report whether the booking
  * was successful or not to the kafkaService.
  * Only if to book and to persist the booking into kafka was successful the service response with success
  */
class CommitService(bookingService: BookingService, kafkaService: KafkaService)(
    implicit val ex: ExecutionContext) {

  def commit(booking: Booking): Future[Successful] =
    bookingService
      .book(booking)
      .map(_ => Successful(true))
      .recover(notSuccessful)
      .flatMap { successful =>
        kafkaService.produceMessage(booking, successful).map(_ => successful)
      }
      .recover(notSuccessful)

  private def notSuccessful: PartialFunction[Throwable, Successful] = {
    case NonFatal(e) =>
      Successful(false)
  }

}
