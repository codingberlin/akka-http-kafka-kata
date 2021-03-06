package service

import java.util.UUID

import model.api._
import model.kafka.{Booking, Successful}
import org.scalatest._
import play.api.libs.json.Json

class ProtocolSpec extends FlatSpec with Matchers {

  import Protocol._

  "Protocol" should "successful write Booking" in {
    val uuid = UUID.randomUUID()
    val booking = Booking(
      bookingId = uuid,
      Successful(true),
      UserId("123abc"),
      Seq(
        Person(
          Firstname("Max"),
          Lastname("Mustermann"),
          Seat("A5")
        )),
      Price(69.99),
      Flightnumber("EZY8124")
    )

    Json.toJson(booking).toString shouldEqual Json.toJson(booking).toString
  }
}
