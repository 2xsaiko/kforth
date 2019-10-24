@file:Suppress("PropertyName", "IMPLICIT_CAST_TO_ANY", "LocalVariableName")

import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.tasks.Jar
import org.gradle.platform.base.Application
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "therealfarfetchd.kforth"
version = "1.0.0"

val kotlin_version: String by extra
val jline_version: String by extra

val mainClass = "therealfarfetchd.kforth.light.ForthKt"

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    val kotlin_version: String by extra
    classpath(kotlinModule("gradle-plugin", kotlin_version))
  }
}

apply {
  plugin("kotlin")
  plugin("application")
}

configure<ApplicationPluginConvention> {
  mainClassName = mainClass
  applicationName = "kforth"
}

repositories {
  mavenCentral()
}

dependencies {
  compile(kotlinModule("stdlib-jre8", kotlin_version))

  // compile("org.jline", "jline", jline_version)

  testCompile("junit:junit:4.12")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
  manifest { attributes["Main-Class"] = mainClass }
  from(configurations.compile.map { if (it.isDirectory) it else zipTree(it) })
}