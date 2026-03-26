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

import me.limansky.scalatestit.TestitFramework.TEST_RUN_PROP_NAME
import sbt.testing.{ Fingerprint, Framework, Runner }

abstract class TestitFramework(inner: Framework) extends Framework {

  override def name: String = s"TestIt-${inner.name}"

  override def fingerprints(): Array[Fingerprint] = inner.fingerprints()

  override def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): Runner = {

    val innerRunner = inner.runner(args, remoteArgs, testClassLoader)

    getTestRunId(args) match {
      case Some(id) => new TestitRunner(innerRunner, id, testClassLoader)
      case None     => innerRunner
    }
  }

  private def getTestRunId(args: Array[String]): Option[String] = {
    val id = args.indexOf("--testRunId")
    val fromArgs = if (id != -1 && id < args.length - 2) Some(args(id + 1)) else None

    fromArgs orElse Option(System.getProperty(TEST_RUN_PROP_NAME)) orElse Option(System.getenv(TEST_RUN_PROP_NAME))
  }
}

object TestitFramework {
  val TEST_RUN_PROP_NAME = "TMS_TEST_RUN_ID"
}
