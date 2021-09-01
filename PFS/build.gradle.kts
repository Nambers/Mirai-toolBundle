plugins {
    val kotlinVersion = "1.5.21"
    kotlin("jvm") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.7.0"
}

group = "tech.eritquearcus"
version = "1.2.0"

repositories {
    mavenLocal()
    mavenCentral()
}
dependencies{
    implementation("com.google.code.gson:gson:2.8.8")
}