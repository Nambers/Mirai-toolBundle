plugins {
    val kotlinVersion = "1.7.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.13.2"
}

group = "tech.eritquearcus"
version = "1.5.0"

repositories {
    //国内镜像源
    mavenLocal()
    mavenCentral()
    maven{ url =uri("https://maven.aliyun.com/nexus/content/groups/public/")}
}
dependencies{
    // Warn: Provides transitive vulnerable dependency
    implementation("com.baidu.aip:java-sdk:4.16.3")
    implementation("com.google.code.gson:gson:2.10")
}