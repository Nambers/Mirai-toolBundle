plugins {
    val kotlinVersion = "1.5.32"
    kotlin("jvm") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.8.3"
}

group = "tech.eritquearcus"
version = "1.3.0"

repositories {
    mavenLocal()
    mavenCentral()
}
dependencies{
    implementation("com.google.code.gson:gson:2.8.9")
}