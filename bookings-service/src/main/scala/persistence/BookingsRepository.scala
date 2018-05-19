package persistence

import model.api.{Booking, UserId}

import scala.concurrent.Future

/**
  * Dummy implementation of any persistence layer
  * The main persistence layer is kafka when its configured to keep messages forever
  * But when a microservice has its own persistence it can work even if kafka is temprorarly not available.
  *
  * This could be an in-memory cache e.g. if the micro service is just interested in windowed data of a short time period
  * or this could be a database.
  */
class BookingsRepository {

  private val bookings = scala.collection.mutable.HashSet.empty[Booking]

  def insert(booking: Booking): Future[Unit] = {
    bookings += booking
    Future.successful(())
  }

  def findByUserId(id: UserId): Future[Set[Booking]] = {
    Future.successful(bookings.filter(_.userId == id).toSet)
  }

}
