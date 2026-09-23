plugins {
    kotlin("jvm") version "2.4.20"
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("GenerateKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
