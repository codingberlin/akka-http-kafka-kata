package service

import model.api._
import model.kafka.{Booking, Successful}
import play.api.libs.json.{Json, Writes}

object Protocol {
  implicit val lastnameWrites = AnyValWrites(Lastname.unapply)
  implicit val firstnameWrites = AnyValWrites(Firstname.unapply)
  implicit val seatWrites = AnyValWrites(Seat.unapply)
  implicit val personWrites = Json.writes[Person]
  implicit val priceWrites = AnyValWrites(Price.unapply)
  implicit val userIdWrites = AnyValWrites(UserId.unapply)
  implicit val flightnumberWrites = AnyValWrites(Flightnumber.unapply)
  implicit val successfullWrites = AnyValWrites(Successful.unapply)
  implicit val bookingWrites = Json.writes[Booking]
}

case class AnyValWrites[I, T](unbox: T => I)(implicit writes: Writes[I])
    extends Writes[T] {
  def writes(value: T) = Json.toJson(unbox(value))
}
