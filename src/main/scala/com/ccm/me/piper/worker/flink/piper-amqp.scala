package com.ccm.me.piper.worker.flink

import java.text.SimpleDateFormat
import java.util

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.tools.json.{JSONReader, JSONWriter}
import org.apache.flink.api.common.serialization.{DeserializationSchema, SerializationSchema}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions

import scala.collection.JavaConverters._

class TaskPiSchema extends DeserializationSchema[TaskComputePiQuery] {

  import org.apache.flink.api.scala._

  val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")

  override def deserialize(message: Array[Byte]): TaskComputePiQuery = {
    val task = new JSONReader()
      .read(new String(message))
      .asInstanceOf[util.HashMap[String, Any]]
      .asScala
      .ensuring { it => it("type") == "compute-pi" }

    TaskComputePiQuery(
      task("id").asInstanceOf[String],
      df.parse(task("createTime").asInstanceOf[String]),
      task("jobId").asInstanceOf[String],
      task.getOrElse("label", "<no label>").asInstanceOf[String],
      task("numSamples") match {
        case n: Integer => n.longValue()
        case s: String => s.toLong
        case x@_ => throw new IllegalArgumentException(s"Expected Integer for numSamples. Got ${x.getClass.getSimpleName}")
      }
    )
  }

  override def isEndOfStream(nextElement: TaskComputePiQuery): Boolean = false

  override def getProducedType: TypeInformation[TaskComputePiQuery] = Types.CASE_CLASS[TaskComputePiQuery]
}

object TaskPiSchema {
  def apply() = new TaskPiSchema()
}

class MessageSchema extends SerializationSchema[WorkerMessage] {
  val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")

  override def serialize(element: WorkerMessage): Array[Byte] = (element match {
    case ts: TaskStartedEvent =>
      new JSONWriter()
        .write(Map(
          "id" -> ts.id.toString,
          "taskId" -> ts.taskId,
          "jobId" -> ts.jobId,
          "createTime" -> df.format(ts.createTime),
          "type" -> ts.`type`,
          "status" -> "STARTED"
        ).asJava)
    case tc: TaskComputePiCompletion =>
      new JSONWriter()
        .write(Map(
          "id" -> tc.taskId,
          "jobId" -> tc.jobId,
          "executionTime" -> tc.executionTime,
          "endTime" -> df.format(tc.endTime),
          "result" -> tc.result,
          "status" -> "COMPLETED"
        ).asJava)
  }).getBytes
}

object MessageSchema {
  def apply() = new MessageSchema()
}


object MessagePublishOptions extends RMQSinkPublishOptions[WorkerMessage] {
  override def computeRoutingKey(a: WorkerMessage): String = a match {
    case _: Event => "events"
    case _ => "completions"
  }

  override def computeProperties(a: WorkerMessage): AMQP.BasicProperties =
    new BasicProperties()
      .builder()
      .contentType("application/json")
      .headers(Map(
        "__TypeId__" -> (a match {
          case _: Event => "com.creactiviti.piper.core.event.PiperEvent"
          case _ => "com.creactiviti.piper.core.task.SimpleTaskExecution"
        }),
        "__KeyTypeId__" -> "java.lang.String",
        "__ContentTypeId__" -> "java.lang.Object")
        .mapValues(_.asInstanceOf[AnyRef])
        .asJava
      )
      .build()

  override def computeExchange(a: WorkerMessage): String = "piper.tasks"
}
