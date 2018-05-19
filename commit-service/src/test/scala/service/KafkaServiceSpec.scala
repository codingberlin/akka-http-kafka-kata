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
import model.kafka.Successful
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._

class KafkaServiceSpec
    extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with KafkaProducerAndConsumer {

  val topicName = TopicName(s"KafkaServiceSpecTopic-${UUID.randomUUID}")

  val anyLastname = Lastname(UUID.randomUUID().toString)
  val anyOtherLastname = Lastname(UUID.randomUUID().toString)
  val anyBooking = ApiBooking(
    UserId("123abc"),
    Seq(
      Person(
        Firstname("Max"),
        anyLastname,
        Seat("A5")
      )),
    Price(69.99),
    Flightnumber("EZY8124"),
    Provider("easyjet")
  )
  val anyOtherBooking = anyBooking.copy(
    persons = Seq(anyBooking.persons.head.copy(lastname = anyOtherLastname)))

  val kafkaService = new KafkaService(topicName, producerSettings)

  "KafkaService" should "send a message to kafka" in {
    val consumedMessagesFuture = for {
      _ <- kafkaService.produceMessage(anyBooking, Successful(true))
      _ <- kafkaService.produceMessage(anyOtherBooking, Successful(false))
      consumedMessages <- consumeMessages(2)
    } yield consumedMessages

    val consumedMessages = Await.result(consumedMessagesFuture, 10.seconds)
    consumedMessages.size shouldEqual 2
    consumedMessages.count { message =>
      message.contains(anyLastname.lastname) && message.contains(
        """"successful":true""")
    } shouldEqual 1
    consumedMessages.count { message =>
      message.contains(anyOtherLastname.lastname) && message.contains(
        """"successful":false""")
    } shouldEqual 1
  }

}
