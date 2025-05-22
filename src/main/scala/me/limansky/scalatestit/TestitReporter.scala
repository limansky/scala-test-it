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

import org.scalatest.Reporter
import org.scalatest.events._
import ru.testit.models.{ ClassContainer, ItemStatus, MainContainer, TestResult }
import ru.testit.services.{ AdapterManager, ConfigManager, ExecutableTest, Utils }

import java.util.{ Properties, UUID }
import org.slf4j.LoggerFactory
import ru.testit.properties.AppProperties

class TestitReporter extends Reporter {

  private lazy val logger = LoggerFactory.getLogger(getClass())
  private val mgr = createAdapterManager
  private val launchId = UUID.randomUUID().toString

  private val executableTest: ThreadLocal[ExecutableTest] = ThreadLocal.withInitial(() => new ExecutableTest)

  override def apply(event: Event): Unit = {
    event match {
      case rs: RunStarting    => runStarting(rs)
      case rc: RunCompleted   => runCompleted(rc)
      case ss: SuiteStarting  => suiteStarting(ss)
      case sc: SuiteCompleted => suiteCompleted(sc)
      case ts: TestStarting   => testStarting(ts)
      case ts: TestSucceeded  => testSucceeded(ts)
      case tf: TestFailed     => testFailed(tf)
      case tc: TestCanceled   => testCanceled(tc)
      case ti: TestIgnored    => testIgnored(ti)
      case _                  => println(s"!!!!!!!!!!! Ignore event $event")
    }
  }

  private def runStarting(rs: RunStarting): Unit = {
    val mc = new MainContainer().setStart(rs.timeStamp).setUuid(launchId)
    mgr.startMainContainer(mc)
    mgr.startTests()
  }

  private def runCompleted(completed: RunCompleted): Unit = {
    mgr.stopTests()
    mgr.stopMainContainer(launchId)
  }

  private def suiteStarting(starting: SuiteStarting): Unit = {
    logger.debug(s"Start suite, ${starting.suiteId} ${starting.suiteName} ")
    val cc = new ClassContainer().setStart(starting.timeStamp).setName(starting.suiteName).setUuid(starting.suiteId)
    mgr.startClassContainer(launchId, cc)
  }

  private def suiteCompleted(completed: SuiteCompleted): Unit = {
    logger.debug(s"Stop suite ${completed.suiteId} : ${completed.suiteName}")
    mgr.updateClassContainer(
      completed.suiteId,
      (cc: ClassContainer) => {
        cc.setStop(completed.timeStamp)
      }
    )
    mgr.stopClassContainer(completed.suiteId)
  }

  private def testStarting(starting: TestStarting): Unit = {
    logger.debug(
      s"Test starting: ${starting.testName}, ${starting.suiteId}, ${starting.suiteClassName}, ${starting.suiteName}"
    )

    var test = executableTest.get()
    if (test.isStarted) {
      test = newTest()
    }
    test.setTestStatus()

    val result = new TestResult()
      .setUuid(test.getUuid)
      .setName(starting.testName)
      .setStart(starting.timeStamp)
      .setTitle(starting.testText)
      .setClassName(starting.suiteName)
      .setSpaceName(starting.suiteClassName.map(pkgName).orNull)
      .setExternalId(Utils.getHash(starting.suiteId + starting.testName))

    mgr.updateClassContainer(starting.suiteId, (cc: ClassContainer) => cc.getChildren.add(test.getUuid))
    mgr.scheduleTestCase(result)
    mgr.startTestCase(test.getUuid)
  }

  private def testSucceeded(succeeded: TestSucceeded): Unit = {
    logger.debug(s"Test succeeded: ${succeeded.testName}")
    stopTest(ItemStatus.PASSED, succeeded.timeStamp, None)
  }

  private def testFailed(failed: TestFailed): Unit = {
    logger.debug(s"Test failed: ${failed.testName}")
    stopTest(ItemStatus.FAILED, failed.timeStamp, failed.throwable)
  }

  private def testCanceled(canceled: TestCanceled): Unit = {
    logger.debug(s"Test canceled ${canceled.testName}")
    stopTest(ItemStatus.BLOCKED, canceled.timeStamp, canceled.throwable)
  }

  private def stopTest(status: ItemStatus, timestamp: Long, reason: Option[Throwable]): Unit = {
    val test = executableTest.get()
    test.setAfterStatus()
    mgr.updateTestCase(
      test.getUuid,
      (result: TestResult) => {
        result.setItemStatus(status)
        result.setStop(timestamp)
        reason.foreach(result.setThrowable)
      }
    )
    mgr.stopTestCase(test.getUuid)
  }

  private def testIgnored(ignored: TestIgnored): Unit = {
    logger.debug(s"Test ignored: ${ignored.testName}")
    val test = newTest()
    test.setTestStatus()

    val result = new TestResult()
      .setUuid(test.getUuid)
      .setName(ignored.testName)
      .setStart(ignored.timeStamp)
      .setTitle(ignored.testText)
      .setClassName(ignored.suiteName)
      .setSpaceName(ignored.suiteClassName.map(pkgName).orNull)
      .setExternalId(Utils.getHash(ignored.suiteId + ignored.testName))

    mgr.updateClassContainer(ignored.suiteId, (cc: ClassContainer) => cc.getChildren.add(test.getUuid))
    mgr.scheduleTestCase(result)
    mgr.startTestCase(test.getUuid)

    test.setAfterStatus()
    mgr.updateTestCase(
      test.getUuid,
      (tr: TestResult) => {
        tr.setItemStatus(ItemStatus.SKIPPED)
        tr.setStop(ignored.timeStamp)
      }
    )
    mgr.stopTestCase(test.getUuid)
  }

  private def newTest(): ExecutableTest = {
    executableTest.remove()
    executableTest.get()
  }

  private def pkgName(s: String): String = {
    val idx = s.lastIndexOf('.')
    if (idx != -1) s.substring(0, idx) else ""
  }

  private val envVariables = Map(
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

  private def createAdapterManager: AdapterManager = {
    val props = new Properties()
    val stream = getClass.getClassLoader.getResourceAsStream("testit.properties")
    if (stream != null) {
      props.load(stream)
    }
    envVariables.foreach {
      case (env, prop) =>
        val v = System.getenv(env)
        if (v != null) props.setProperty(prop, v)
    }
    val cfgManager = new ConfigManager(props)
    new AdapterManager(cfgManager.getClientConfiguration, cfgManager.getAdapterConfig)
  }
}
