/*
The MIT License (MIT)
Copyright (c) 2017 Chris Camel
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.ccm.me.piper.worker.flink

import java.lang.System.currentTimeMillis

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{OutputTag, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig
import org.apache.flink.streaming.connectors.rabbitmq.{RMQSink, RMQSource}
import org.apache.flink.util.Collector

object Job {

  case class UnitOfWork(
                         jobId: String,
                         taskId: String,
                         numSamples: Long,
                         iteration: Long,
                         currentResult: Long) {
    val startTimestamp: Long = currentTimeMillis
  }

  def main(args: Array[String]) {
    // Checking input parameters
    val params = ParameterTool.fromArgs(args)

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val connectionConfig =
      new RMQConnectionConfig.Builder()
        .setUri("amqp://guest:guest@localhost:5672")
        .build

    env.setParallelism(8)

    val outputTag = OutputTag[WorkerMessage]("side-output")

    val input = env
      .addSource(new RMQSource(connectionConfig, "flink", true, new TaskPiSchema()))
      .name("task")

    val taskDs =
      input
        .process(new ProcessFunction[TaskComputePiQuery, TaskComputePiQuery] {
          override def processElement(
                                       value: TaskComputePiQuery,
                                       ctx: ProcessFunction[TaskComputePiQuery, TaskComputePiQuery]#Context,
                                       out: Collector[TaskComputePiQuery]): Unit = {
            ctx.output(outputTag, TaskStartedEvent(value.taskId, value.jobId))
            out.collect(value)
          }
        })

    val processDs =
      taskDs.flatMap {
        (task: TaskComputePiQuery, out: Collector[UnitOfWork]) =>
          (1L to task.numSamples)
            .map {
              UnitOfWork(task.jobId, task.taskId, task.numSamples, _, 0)
            }
            .foreach {
              out.collect
            }
      }
        .map { value: UnitOfWork => {
          val x = Math.random()
          val y = Math.random()

          value.copy(currentResult = if (x * x + y * y < 1) 1L else 0L)
        }
        }
        .keyBy(_.taskId)
        .countWindow(1)
        .trigger(CountTrigger.of(100000))
        .reduce((a, b) => a.copy(currentResult = a.currentResult + b.currentResult))


    taskDs
      .getSideOutput(outputTag)
      .addSink(new RMQSink[WorkerMessage](
        connectionConfig,
        MessageSchema(),
        MessagePublishOptions
      ))

    processDs
      .map { w => TaskComputePiCompletion(w.taskId, w.jobId, currentTimeMillis() - w.startTimestamp, w.currentResult * 4.0 / w.numSamples).asInstanceOf[WorkerMessage] }
      .addSink(new RMQSink[WorkerMessage](
        connectionConfig,
        MessageSchema(),
        MessagePublishOptions
      ))

    env.execute("Compute Pi Job")
  }
}
