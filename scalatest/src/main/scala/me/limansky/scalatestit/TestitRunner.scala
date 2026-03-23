package me.limansky.scalatestit

import sbt.testing.{ Runner, Task, TaskDef }

import scala.jdk.CollectionConverters._

class TestitRunner(inner: Runner, testRunId: String) extends Runner {

  private val mgr = TestItUtils.createAdapterManager(Some(testRunId))

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    val ids = mgr.getTestFromTestRun.asScala

    println(s"GOT IDS $ids")
    ???
  }

  override def done(): String = {
    inner.done()
  }

  override def remoteArgs(): Array[String] = inner.remoteArgs()

  override def args(): Array[String] = inner.args()
}
