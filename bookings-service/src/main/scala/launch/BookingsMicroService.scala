package launch

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.stream.ActorMaterializer
import controller.BookingsController
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{
  ByteArrayDeserializer,
  StringDeserializer
}
import persistence.BookingsRepository
import service.{KafkaService, TopicName}

object CommitMicroService extends App {

  implicit val system = ActorSystem("CommitMicroService")
  implicit val materializer = ActorMaterializer()
  implicit val ex = system.dispatcher

  // in real microservice this should come out of a configuration
  val kafkaServer = "localhost:9092"
  val kafkaTopicName = TopicName("bookingsMicroService")
  val interface = "localhost"
  val port = 9001

  val bookingsRepository = new BookingsRepository()

  val kafkaConsumerSettings =
    ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaServer)
      .withGroupId("CommitMicroService")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  val kafkaService =
    new KafkaService(bookingsRepository, kafkaTopicName, kafkaConsumerSettings)
  val bookingsController = new BookingsController(bookingsRepository)

  bookingsController
    .start(interface, port)
    .onComplete(_ => system.terminate())
}
