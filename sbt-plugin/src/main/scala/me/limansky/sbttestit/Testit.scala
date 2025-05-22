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

package me.limansky.sbttestit

import sbt.*
import sbt.Keys.*

object TestItPlugin extends AutoPlugin {
  object autoImport {
    val testItEnabled = settingKey[Boolean]("Enable test it reports")
  }

  override val trigger = noTrigger

  import autoImport.*

  override lazy val projectSettings: Seq[Setting[?]] = Seq(
    Test / testItEnabled := true,
    Test / testOptions ++= {
      if ((Test / testItEnabled).value)
        Seq(Tests.Argument("-C", "me.limansky.scalatestit.TestitReporter"))
      else Seq.empty
    },
    libraryDependencies ++= {
      if ((Test / testItEnabled).value)
        Seq(
          "me.limansky" %% "scala-test-it" % BuildInfo.version
        )
      else Seq.empty
    }
  )
}
