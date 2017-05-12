
lazy val console = project
  .settings(
    name := "hs",
    libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1"
  )

lazy val gui = project
  .settings(
    unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar")))
  .dependsOn(console)

fork in run := true

lazy val root = project.in(file("."))
  .dependsOn(gui, console)

