package model.kafka

import model.AnyValFormat
import model.api.{Flightnumber, UserId}
import play.api.libs.json._

object Protocol {
  implicit val lastnameFormat = AnyValFormat(Lastname)(Lastname.unapply)
  implicit val firstnameFormat = AnyValFormat(Firstname)(Firstname.unapply)
  implicit val seatFormat = AnyValFormat(Seat)(Seat.unapply)
  implicit val personFormat = Json.format[Person]
  implicit val userIdFormat = AnyValFormat(UserId)(UserId.unapply)
  implicit val flightnumberFormat =
    AnyValFormat(Flightnumber)(Flightnumber.unapply)
  implicit val successfulFormat = AnyValFormat(Successful)(Successful.unapply)
  implicit val priceFormat = AnyValFormat(Price)(Price.unapply)
  implicit val bookingFormat = Json.format[Booking]
}
