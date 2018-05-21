package service

import java.util.UUID

import model.api.{
  Firstname,
  Flightnumber,
  Lastname,
  Person,
  Price,
  Provider,
  Seat,
  UserId,
  Booking => ApiBooking
}
import model.kafka.{Successful, Booking => KafkaBooking}
import org.scalatest._
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration._
import Protocol._

class KafkaServiceSpec
    extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with KafkaProducerAndConsumer {

  val topicName = TopicName(s"KafkaServiceSpecTopic-${UUID.randomUUID}")

  val kafkaService = new KafkaService(topicName, producerSettings)

  "KafkaService" should "send a message to kafka" in {
    val consumedMessagesFuture = for {
      _ <- kafkaService.produceMessage(anyBooking, Successful(true))
      _ <- kafkaService.produceMessage(anyOtherBooking, Successful(false))
      consumedMessages <- consumeMessages(2)
    } yield consumedMessages

    val consumedMessages = Await.result(consumedMessagesFuture, 10.seconds)
    consumedMessages.size shouldEqual 2
    consumedMessages.count(
      filterUserIdAndSuccessful(anyUserId, Successful(true))) shouldEqual 1
    consumedMessages.count(filterUserIdAndSuccessful(
      anyOtherUserId,
      Successful(false))) shouldEqual 1
  }

  private def filterUserIdAndSuccessful(
      expectedUserId: UserId,
      expectedSuccessful: Successful): String => Boolean = { message =>
    Json
      .parse(message)
      .validate[KafkaBooking]
      .map { booking =>
        booking.userId == expectedUserId && booking.successful == expectedSuccessful
      }
      .getOrElse(false)
  }

  val anyUserId = UserId(UUID.randomUUID().toString)
  val anyOtherUserId = UserId(UUID.randomUUID().toString)
  val anyBooking = ApiBooking(
    anyUserId,
    Seq(
      Person(
        Firstname("Max"),
        Lastname("Mustermann"),
        Seat("A5")
      )),
    Price(69.99),
    Flightnumber("EZY8124"),
    Provider("easyjet")
  )
  val anyOtherBooking = anyBooking.copy(userId = anyOtherUserId)
}
