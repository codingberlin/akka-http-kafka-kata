package service

import controller.AnyValFormat
import model.api._
import model.kafka.{Booking, Successful}
import play.api.libs.json.{Json, Writes}

object Protocol {
  implicit val lastnameWrites = AnyValFormat(Lastname)(Lastname.unapply)
  implicit val firstnameWrites = AnyValFormat(Firstname)(Firstname.unapply)
  implicit val seatWrites = AnyValFormat(Seat)(Seat.unapply)
  implicit val personWrites = Json.format[Person]
  implicit val priceWrites = AnyValFormat(Price)(Price.unapply)
  implicit val userIdWrites = AnyValFormat(UserId)(UserId.unapply)
  implicit val flightnumberWrites =
    AnyValFormat(Flightnumber)(Flightnumber.unapply)
  implicit val successfullWrites = AnyValFormat(Successful)(Successful.unapply)
  implicit val bookingWrites = Json.format[Booking]
}
