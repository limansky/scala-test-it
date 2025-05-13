package me.limansky.scalatestit

import org.scalatest.Reporter
import org.scalatest.events._
import ru.testit.models.{ ClassContainer, ItemStatus, MainContainer, TestResult }
import ru.testit.services.{ AdapterManager, ConfigManager, ExecutableTest, Utils }

import java.util.{ Properties, UUID }
import org.slf4j.LoggerFactory

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
    val test = executableTest.get()
    test.setAfterStatus()
    mgr.updateTestCase(
      test.getUuid,
      (result: TestResult) => {
        result.setItemStatus(ItemStatus.PASSED)
        result.setStop(succeeded.timeStamp)
      }
    )
    mgr.stopTestCase(test.getUuid)
  }

  private def testFailed(failed: TestFailed): Unit = {
    logger.debug(s"Test failed: ${failed.testName}")
    val test = executableTest.get()
    test.setAfterStatus()
    mgr.updateTestCase(
      test.getUuid,
      (result: TestResult) => {
        result.setItemStatus(ItemStatus.FAILED)
        result.setStop(failed.timeStamp)
        failed.throwable.foreach(result.setThrowable)
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

  private def createAdapterManager: AdapterManager = {
    val props = new Properties()
    val stream = getClass.getClassLoader.getResourceAsStream("testit.properties")
    props.load(stream)
    val cfgManager = new ConfigManager(props)
    new AdapterManager(cfgManager.getClientConfiguration, cfgManager.getAdapterConfig)
  }
}
