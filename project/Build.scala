/*
The MIT License (MIT)

Copyright (c) 2014 Steve Wagner (aka ciroque)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

import sbt._
import sbt.Keys._
import sbt.ScmInfo
import scala.Some

object ApplicationBuild extends Build {

  val appName = "securesocial-mongodb-userservice"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "ws.securesocial" %% "securesocial" % "2.1.3"
    , "org.reactivemongo" %% "reactivemongo" % "0.10.0" exclude("org.scala-stm", "scala-stm_2.10.0")
  )

  override lazy val settings = super.settings ++ Seq(
    organization := "org.ciroque",
    version := appVersion,

    publishMavenStyle := true,
    publishArtifact in Test := false,

    publishTo <<= version {
      (v: String) =>
        val nexus = "http://amala.wagner-x.net:8081/nexus/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
        else Some("releases" at nexus + "content/repositories/releases")
    },

    pomIncludeRepository := {
      x => true
    },
    licenses += ("MIT" -> url("http://opensource.org/licenses/MIT")),
    homepage := Some(url("http://ciroque-x.net/")),
    scmInfo := Some(ScmInfo(url("http://ciroque-x.net"), "https://github.com/ciroque/securesocial-mongodb-userservice.git")),

    // Maven central wants some extra metadata to keep things 'clean'.
    pomExtra := (
      <developers>
        <developer>
          <id>ciroque</id>
          <name>Steve Wagner</name>
        </developer>
      </developers>)
  )
}
