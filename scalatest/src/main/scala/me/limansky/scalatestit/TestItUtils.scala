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

import ru.testit.services.{ AdapterManager, ConfigManager }

import java.util.Properties

object TestItUtils {
  val ENV_VARIABLES: Map[String, String] = Map(
    "TMS_PRIVATE_TOKEN" -> "privateToken",
    "TMS_URL" -> "url",
    "TMS_PROJECT_ID" -> "projectId",
    "TMS_CONFIGURATION_ID" -> "configurationId",
    "TMS_TEST_RUN_ID" -> "testRunId",
    "TMS_TEST_RUN_NAME" -> "testRunName",
    "TMS_ADAPTER_MODE" -> "adapterMode",
    "TMS_CERT_VALIDATION" -> "certValidation",
    "TMS_AUTOMATIC_CREATION_TEST_CASES" -> "automaticCreationTestCases",
    "AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES" -> "automaticUpdationLinksToTestCases",
    "TMS_TEST_IT" -> "testIt",
    "TMS_IMPORT_REALTIME" -> "importRealtime"
  )

  def createAdapterManager(testRunId: Option[String] = None): AdapterManager = {
    val props = new Properties()
    val stream = getClass.getClassLoader.getResourceAsStream("testit.properties")
    if (stream != null) {
      props.load(stream)
    }
    ENV_VARIABLES.foreach {
      case (env, prop) =>
        val sysPropName = "tms" + prop.capitalize
        val sv = Option(System.getProperty(sysPropName))
        val ev = Option(System.getenv(env))
        sv.orElse(ev).foreach(v => props.setProperty(prop, v))
    }

    testRunId.foreach(id => props.setProperty("TEMS_TEST_RUN_ID", id))

    val cfgManager = new ConfigManager(props)
    new AdapterManager(cfgManager.getClientConfiguration, cfgManager.getAdapterConfig)
  }
}
