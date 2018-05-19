package controller

import model.api._
import org.scalatest._
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}

class ProtocolSpec extends FlatSpec with Matchers {

  import Protocol._

  "Protocol" should "successful parse json to AnyVal" in {
    parseAnyVal(""""Max"""", "Max")(Firstname)
    parseAnyVal(""""Mustermann"""", "Mustermann")(Lastname)
    parseAnyVal(""""A5"""", "A5")(Seat)
    parseAnyVal(""""EZY8124"""", "EZY8124")(Flightnumber)
    parseAnyVal("""69.99""", 69.99)(Price)
    parseAnyVal(""""easyjet"""", "easyjet")(Provider)
    parseAnyVal(""""123abc"""", "123abc")(Provider)
  }

  it should "reject empty string for String AnyVal" in {
    rejectParsing("""""""")(Firstname)
    rejectParsing("""""""")(Lastname)
    rejectParsing("""""""")(Seat)
    rejectParsing("""""""")(Flightnumber)
    rejectParsing("""""""")(Provider)
    rejectParsing("""""""")(Provider)
  }

  it should "reject Seat with wrong format" in {
    Seq("""""""", """"1"""", """"A"""", """"11"""", """"AA"""")
      .map(Json.parse)
      .map(_.validate[Seat])
      .map {
        case JsSuccess(seat, _) =>
          fail(s"parsed successfully $seat but should have failed")
        case e: JsError =>
          succeed
      }
  }

  it should "parse Booking successful" in {
    val bookingJson = """{
                        |"userId": "123abc",
                        |"persons": [{
                        |  "firstname": "Max",
                        |  "lastname": "Mustermann",
                        |  "seat": "A5"
                        |}],
                        |"price": 69.99,
                        |"flightnumber": "EZY8124",
                        |"provider": "easyjet"
                        |}""".stripMargin

    Json.parse(bookingJson).validate[Booking] shouldEqual JsSuccess(
      Booking(
        UserId("123abc"),
        Seq(
          Person(
            Firstname("Max"),
            Lastname("Mustermann"),
            Seat("A5")
          )),
        Price(69.99),
        Flightnumber("EZY8124"),
        Provider("easyjet")
      ))
  }

  it should "write ValidationErrors" in {
    val result = Json
      .toJson(
        ValidationErrors(Path("somePath"), Seq(ValidationError("someError"))))
      .toString

    result shouldEqual """{"path":"somePath","errors":["someError"]}"""
  }

  def parseAnyVal[T: Reads, I](givenJson: String, expectedValue: I)(
      box: I => T): Unit = {
    Json.parse(givenJson).validate[T] match {
      case JsSuccess(value, _) =>
        value shouldEqual box(expectedValue)
      case e: JsError =>
        fail(s"tried to parse '$expectedValue' but failed with $e")
    }
  }

  def rejectParsing[T: Reads, I](givenJson: String)(box: I => T): Unit = {
    Json.parse(givenJson).validate[T] match {
      case s: JsSuccess[T] =>
        fail(s"parsed successfully empty string but should have failed")
      case e: JsError =>
        ()
    }
  }
}
