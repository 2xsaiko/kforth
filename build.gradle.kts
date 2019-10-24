@file:Suppress("PropertyName")

import org.gradle.api.tasks.JavaExec
import org.gradle.platform.base.Application
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "therealfarfetchd.kforth"
version = "1.0.0"

val kotlin_version: String by extra
val jline_version: String by extra

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

tasks.withType<JavaExec> {
  main = "therealfarfetchd.kforth.light.ForthKt"
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