package service

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import model.kafka.Booking
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{
  ByteArraySerializer,
  StringSerializer
}
import org.scalatest.BeforeAndAfterAll
import play.api.libs.json.Json
import model.kafka.Protocol._

trait KafkaProducer { self: BeforeAndAfterAll =>
  def topicName: TopicName

  implicit val system = ActorSystem("KafkaProducer")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val producerSettings =
    ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")

  val producerSink = Producer.plainSink(producerSettings)

  def produceMessage(booking: Booking) = {
    val payload = Json.toJson(booking).toString

    Source(1 to 1)
      .map { _ =>
        new ProducerRecord[Array[Byte], String](topicName.topicName, payload)
      }
      .runWith(producerSink)
      .map(_ => ())
  }

  override def afterAll(): Unit = {
    system.terminate()
    ()
  }
}
