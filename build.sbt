
lazy val cmdline = project.in(file("console"))
  .settings(
    name := "hs",
    libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1",
    mainClass in (Compile, run) := Some("learner.learn"),
   	mainClass in assembly := Some("learner.learn"),
    assemblyOutputPath in assembly := file("./console.jar")
  )

lazy val gui = project
  .settings(
  	mainClass in assembly := Some("sample.GUI"),
    assemblyOutputPath in assembly := file("./gui.jar"),
    unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar"))
    )
  .dependsOn(cmdline)

fork in run := true

lazy val root = project.in(file("."))
  .dependsOn(gui, cmdline)

compile <<= (compile in Compile)
  .dependsOn(assembly in gui, assembly in cmdline)