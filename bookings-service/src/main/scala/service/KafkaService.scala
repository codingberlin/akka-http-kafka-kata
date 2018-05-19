package service

import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import persistence.BookingsRepository
import play.api.libs.json.{JsError, JsSuccess, Json}
import model.kafka.{Booking => KafkaBooking}
import model.api.{NumberOfPersons, Booking => ApiBooking}
import model.kafka.Protocol._

import scala.concurrent.{ExecutionContext, Future}

class KafkaService(bookingsRepository: BookingsRepository,
                   topicName: TopicName,
                   consumerSettings: ConsumerSettings[Array[Byte], String])(
    implicit val materializer: ActorMaterializer,
    ex: ExecutionContext) {

  private val subscription = Subscriptions.assignmentWithOffset(
    new TopicPartition(topicName.topicName, 0),
    0)

  Consumer
    .plainSource(consumerSettings, subscription)
    .map { record: ConsumerRecord[Array[Byte], String] =>
      Json.parse(record.value).validate[KafkaBooking]
    }
    .collect {
      case JsSuccess(kafkaBooking, _) if kafkaBooking.successful.successful =>
        kafkaBooking
    }
    .map { kafkaBooking =>
      ApiBooking(kafkaBooking.bookingId,
                 kafkaBooking.userId,
                 NumberOfPersons(kafkaBooking.persons.size),
                 kafkaBooking.flightnumber)
    }
    .mapAsync(1)(bookingsRepository.insert)
    .runWith(Sink.ignore)

}
