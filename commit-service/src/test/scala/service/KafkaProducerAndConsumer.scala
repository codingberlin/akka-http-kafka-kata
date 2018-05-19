package service

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{
  ByteArrayDeserializer,
  ByteArraySerializer,
  StringDeserializer,
  StringSerializer
}
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Future

trait KafkaProducerAndConsumer { self: BeforeAndAfterAll =>
  def topicName: TopicName

  implicit val system = ActorSystem("KafkaServiceSpec")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val producerSettings =
    ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")

  def consumeMessages(amount: Int): Future[List[String]] = {
    val consumerSettings = ConsumerSettings(system,
                                            new ByteArrayDeserializer,
                                            new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("PlainSourceConsumer")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val subscription = Subscriptions.assignmentWithOffset(
      new TopicPartition(topicName.topicName, 0),
      0)
    Consumer
      .plainSource(consumerSettings, subscription)
      .take(2)
      .runWith(Sink.fold(List.empty[String]) {
        (messages, record: ConsumerRecord[Array[Byte], String]) =>
          record.value +: messages
      })
  }
  override def afterAll(): Unit = {
    system.terminate()
    ()
  }
}
