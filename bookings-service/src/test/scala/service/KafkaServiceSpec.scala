package service

import java.util.UUID

import akka.kafka.ConsumerSettings
import model.api.{Flightnumber, NumberOfPersons, UserId, Booking => ApiBooking}
import model.kafka.{
  Firstname,
  Lastname,
  Person,
  Price,
  Seat,
  Successful,
  Booking => KafkaBooking
}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{
  ByteArrayDeserializer,
  StringDeserializer
}
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import persistence.BookingsRepository
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatest.concurrent.Eventually

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class KafkaServiceSpec
    extends FlatSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll
    with KafkaProducer
    with Eventually {

  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(5.seconds), interval = scaled(1.seconds))

  val topicName = TopicName(s"KafkaServiceSpecTopic-${UUID.randomUUID}")

  val anySuccessfulBooking = KafkaBooking(
    UUID.randomUUID(),
    Successful(true),
    UserId("123abc"),
    Seq(
      Person(
        Firstname("Max"),
        Lastname("Mustermann"),
        Seat("A5")
      )),
    Price(69.99),
    Flightnumber("EZY8124"),
  )
  val anyNotSuccessfulBooking = anySuccessfulBooking.copy(
    successful = Successful(false),
    userId = UserId("1701"))

  "KafkaService" should "store successful booking only" in {
    val (kafkaService, bookingsRepositoriy) = kafkaServiceAndMocks
    when(bookingsRepositoriy.insert(any())).thenReturn(Future.successful(()))

    Await.ready(produceMessage(anyNotSuccessfulBooking), 5.seconds)
    Await.ready(produceMessage(anySuccessfulBooking), 5.seconds)

    eventually {
      verify(bookingsRepositoriy, times(1)).insert(any())
      verify(bookingsRepositoriy, times(1)).insert(
        ApiBooking(anySuccessfulBooking.bookingId,
                   anySuccessfulBooking.userId,
                   NumberOfPersons(anySuccessfulBooking.persons.size),
                   anySuccessfulBooking.flightnumber))
    }
  }

  private def kafkaServiceAndMocks = {
    val consumerSettings = ConsumerSettings(system,
                                            new ByteArrayDeserializer,
                                            new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("PlainSourceConsumer")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val bookingsRepository = mock[BookingsRepository]
    val kafkaService =
      new KafkaService(bookingsRepository, topicName, consumerSettings)

    (kafkaService, bookingsRepository)
  }
}
