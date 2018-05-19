package model.kafka

import model.api.{Flightnumber, UserId}
import play.api.libs.json._

object Protocol {
  implicit val lastnameReads = AnyValFormat(Lastname)(Lastname.unapply)
  implicit val firstnameReads = AnyValFormat(Firstname)(Firstname.unapply)
  implicit val seatReads = AnyValFormat(Seat)(Seat.unapply)
  implicit val personReads = Json.format[Person]
  implicit val userIdReads = AnyValFormat(UserId)(UserId.unapply)
  implicit val flightnumberReads =
    AnyValFormat(Flightnumber)(Flightnumber.unapply)
  implicit val successfulReads = AnyValFormat(Successful)(Successful.unapply)
  implicit val priceReads = AnyValFormat(Price)(Price.unapply)
  implicit val bookingReads = Json.format[Booking]
}

case class AnyValFormat[I, T](box: I => T)(unbox: T => Option[I])(
    implicit reads: Reads[I],
    writes: Writes[I])
    extends Reads[T]
    with Writes[T] {
  def reads(js: JsValue) = js.validate[I] map box
  def writes(value: T) = Json.toJson(unbox(value))
}
