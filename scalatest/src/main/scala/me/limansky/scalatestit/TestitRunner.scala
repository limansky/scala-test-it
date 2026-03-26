/*
 * Copyright 2025 Mike Limansky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.limansky.scalatestit

import org.scalatest.Suite
import ru.testit.services.Utils
import sbt.testing.{ Runner, Task, TaskDef, TestSelector }

import scala.jdk.CollectionConverters._

class TestitRunner(inner: Runner, testRunId: String, classLoader: ClassLoader) extends Runner {

  private val mgr = TestItUtils.createAdapterManager(Some(testRunId))

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    val ids = mgr.getTestFromTestRun.asScala.toSet
    val newTaskDefs = updateTaskDefs(taskDefs, ids)
    inner.tasks(newTaskDefs)
  }

  override def done(): String = {
    inner.done()
  }

  override def remoteArgs(): Array[String] = inner.remoteArgs()

  override def args(): Array[String] = inner.args()

  private def updateTaskDefs(taskDefs: Array[TaskDef], ids: Set[String]): Array[TaskDef] = {
    taskDefs.flatMap { td =>
      val className = td.fullyQualifiedName()
      val suiteClass = Class.forName(className, true, classLoader)
      val suite = suiteClass.getDeclaredConstructor().newInstance().asInstanceOf[Suite]
      val testNames = suite.testNames.map(name => Utils.getHash(suite.suiteId + name) -> name).toMap

      val selectors = testNames.filter(kv => ids(kv._1)).map(kv => new TestSelector(kv._2))

      if (selectors.nonEmpty) {
        Some(new TaskDef(td.fullyQualifiedName(), td.fingerprint(), td.explicitlySpecified(), selectors.toArray))
      } else None
    }
  }
}
