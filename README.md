# scala-test-it

[![Build Status](https://github.com/limansky/scala-test-it/actions/workflows/ci.yaml/badge.svg)](https://github.com/limansky/scala-test-it/actions/workflows/ci.yaml)

[ScalaTest][scalatest] and [TestIt][testit] integration.

> [!WARNING]
> The project is in the early stage of devepment.  Lot of functionality isn't supported yet.
> API is not stable, however as an end user you need to use only a small part of it.

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

Test IT settings can be passed via `testit.properties` file in the resources, or via environment variables,
or via system properties.  The system properties have biggest priority, and the file has the smallest.
You can also mix all of the approaches, for example, if you don't want to store private keys in the repository.

You can find settings description at [TestIt JUnit 5 Adapter readme](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-junit5#configuration).


[scalatest]: https://www.scalatest.org/
[testit]: https://testit.software/
