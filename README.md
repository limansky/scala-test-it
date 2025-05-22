# scala-test-it

[![Build Status](https://github.com/limansky/scala-test-it/actions/workflows/ci.yaml/badge.svg)](https://github.com/limansky/scala-test-it/actions/workflows/ci.yaml)

[ScalaTest][scalatest] and [TestIt][testit] integration.

The simplest way to use this is via sbt plugin.  Both sbt 1.x and upcoming 2.x are supported.

```
addSbtPlugin("me.limansky" % "sbt-test-it" % "(version)")
```

The plugin should be enabled manually:

```scala
lazy val myProject = (project in file ("."))
  .enablePlugins(TestItPlugin)
```

TestItPlugin set the reporter dependency for you, and pass the reporter as a parameter to ScalaTest.
It has one setting `testItEnabled` (`true` by default) determine if the test run must be published
to Test IT.

Test IT settings can be passed via `testit.properties` file in the resources, or via environment variables.
You can also mix both approaches, for example, if you don't want to store private keys in the repository.


[scalatest]: https://www.scalatest.org/
[testit]: https://testit.software/
