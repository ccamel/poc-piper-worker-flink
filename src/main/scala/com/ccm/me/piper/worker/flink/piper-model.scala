package com.ccm.me.piper.worker.flink

import java.util.{Date, UUID}

sealed trait CoordinatorMessage

case class TaskComputePiQuery(taskId: String,
                              createTime: Date,
                              jobId: String,
                              label: String,
                              numSamples: Long) extends CoordinatorMessage

object TaskComputePiQuery {
  val TYPE = "compute-pi"
}

sealed trait WorkerMessage

sealed trait Event extends WorkerMessage {
  val id: UUID
  val createTime: Date
  val `type`: String
  val taskId: String
  val jobId: String
}

case class TaskStartedEvent(taskId: String,
                            jobId: String) extends Event {
  val id: UUID = UUID.randomUUID()
  val createTime = new Date()
  val `type`: String = TaskStartedEvent.TYPE
}

object TaskStartedEvent {
  val TYPE = "task.started"
}


case class TaskComputePiCompletion(taskId: String,
                                   jobId: String,
                                   executionTime: Long,
                                   result: Double
                                  ) extends WorkerMessage {
  val endTime = new Date()
}
