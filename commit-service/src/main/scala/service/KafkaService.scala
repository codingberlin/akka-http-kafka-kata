package service

import java.util.{UUID}

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import model.api.{Booking => ApiBooking}
import model.kafka.{Successful, Booking => KafkaBooking}
import org.apache.kafka.clients.producer.{ProducerRecord}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

class KafkaService(topicName: TopicName,
                   producerSettings: ProducerSettings[Array[Byte], String])(
    implicit val materializer: ActorMaterializer,
    ex: ExecutionContext) {
  val producerSink = Producer.plainSink(producerSettings)

  def produceMessage(booking: ApiBooking,
                     successful: Successful): Future[Unit] = {
    import Protocol._

    val kafkaBooking = KafkaBooking(
      UUID.randomUUID(),
      successful,
      booking.userId,
      booking.persons,
      booking.price,
      booking.flightnumber
    )
    val payload = Json.toJson(kafkaBooking).toString

    Source(1 to 1)
      .map { _ =>
        new ProducerRecord[Array[Byte], String](topicName.topicName, payload)
      }
      .runWith(producerSink)
      .map(_ => ())
  }

}
