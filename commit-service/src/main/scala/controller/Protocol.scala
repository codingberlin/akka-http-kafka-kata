package controller

import model.api._
import play.api.libs.json._

object Protocol {
  implicit val lastnameReads = NonEmptyStringAnyValReads(Lastname)
  implicit val firstnameReads = NonEmptyStringAnyValReads(Firstname)
  implicit val seatReads = new Reads[Seat] {
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
  }
  implicit val personReads = Json.reads[Person]

  implicit val userIdReads = NonEmptyStringAnyValReads(UserId)
  implicit val flightnumberReads = NonEmptyStringAnyValReads(Flightnumber)
  implicit val providerReads = NonEmptyStringAnyValReads(Provider)
  implicit val priceReads = AnyValReads(Price)
  implicit val bookingReads = Json.reads[Booking]

  implicit val pathWrites = AnyValWrites(Path.unapply)
  implicit val validationErrorWrites = AnyValWrites(ValidationError.unapply)
  implicit val validationErrorsWrites = Json.writes[ValidationErrors]

}

case class AnyValReads[I, T](box: I => T)(implicit reads: Reads[I])
    extends Reads[T] {
  def reads(js: JsValue) = js.validate[I] map box
}

case class AnyValWrites[I, T](unbox: T => I)(implicit writes: Writes[I])
    extends Writes[T] {
  def writes(value: T) = Json.toJson(unbox(value))
}

case class NonEmptyStringAnyValReads[String, T](box: String => T)(
    implicit reads: Reads[String])
    extends Reads[T] {
  def nonEmpty(value: String): JsResult[String] = value match {
    case "" =>
      JsError("must not be empty")
    case success =>
      JsSuccess(success)
  }

  def reads(js: JsValue) =
    js.validate[String]
      .flatMap(nonEmpty)
      .map(box)
}
