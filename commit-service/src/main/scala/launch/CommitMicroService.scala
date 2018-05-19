package launch

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.stream.ActorMaterializer
import controller.CommitController
import org.apache.kafka.common.serialization.{
  ByteArraySerializer,
  StringSerializer
}
import service.{BookingService, CommitService, KafkaService, TopicName}

object CommitMicroService extends App {

  implicit val system = ActorSystem("CommitMicroService")
  implicit val materializer = ActorMaterializer()
  implicit val ex = system.dispatcher

  // in real microservice this should come out of a configuration
  val kafkaServer = "localhost:9092"
  val kafkaTopicName = TopicName("commitMicroService")
  val interface = "localhost"
  val port = 9000

  val bookingService = new BookingService()

  val kafkaProducerSettings =
    ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(kafkaServer)
  val kafkaService = new KafkaService(kafkaTopicName, kafkaProducerSettings)
  val commitService = new CommitService(bookingService, kafkaService)
  val commitController = new CommitController(commitService)

  commitController
    .start(interface, port)
    .onComplete(_ => system.terminate())
}
