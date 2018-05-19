package model.api

import model.AnyValFormat
import play.api.libs.json._

object Protocol {
  implicit val userIdFormat = AnyValFormat(UserId)(UserId.unapply)
  implicit val flightnumberFormat =
    AnyValFormat(Flightnumber)(Flightnumber.unapply)
  implicit val numberOfPersonsFormat =
    AnyValFormat(NumberOfPersons)(NumberOfPersons.unapply)
  implicit val bookingFormat = Json.format[Booking]
}
