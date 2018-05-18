package service

import model.api.Booking

import scala.concurrent.Future

/* In a real Product this service would determine which provider API must be called and call it
 * But for this code kata it just doest nothing or fails the Future when 3 persons were booked
 * */
class BookingService {

  def book(booking: Booking): Future[Unit] =
    if (booking.persons.size == 3)
      Future.failed(
        new Exception(
          "Dummy service throws exception when exactly 3 persons were booked"))
    else
      Future.successful(())

}
