package controller

import model.api._
import play.api.libs.json._

object Protocol {
  implicit val lastnameReads =
    NonEmptyStringAnyValFormat(Lastname)(Lastname.unapply)
  implicit val firstnameReads =
    NonEmptyStringAnyValFormat(Firstname)(Firstname.unapply)
  implicit val seatReads = new Format[Seat] {
    def reads(js: JsValue) =
      js.validate[String]
        .flatMap {
          case seat if seat.length < 2 =>
            JsError("seat must have at least one letter followed by one digit")
          case seat if !seat.head.isLetter =>
            JsError("first char must be a letter")
          case seat if !seat.tail.forall(_.isDigit) =>
            JsError("every other char than the first must be digits")
          case seat =>
            JsSuccess(seat)

        }
        .map(Seat)
    def writes(seat: Seat) = Json.toJson(seat.seat)
  }
  implicit val personReads = Json.format[Person]

  implicit val userIdReads = NonEmptyStringAnyValFormat(UserId)(UserId.unapply)
  implicit val flightnumberReads =
    NonEmptyStringAnyValFormat(Flightnumber)(Flightnumber.unapply)
  implicit val providerReads =
    NonEmptyStringAnyValFormat(Provider)(Provider.unapply)
  implicit val priceReads = AnyValFormat(Price)(Price.unapply)
  implicit val bookingReads = Json.format[Booking]

  implicit val pathWrites = AnyValFormat(Path)(Path.unapply)
  implicit val validationErrorWrites =
    AnyValFormat(ValidationError)(ValidationError.unapply)
  implicit val validationErrorsWrites = Json.format[ValidationErrors]

}
