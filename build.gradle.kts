plugins {
    val kotlinVersion = "1.5.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.7.0"
}

group = "tech.eritquearcus"
version = "1.3.0"

repositories {
    //国内镜像源
    mavenLocal()
    mavenCentral()
    maven{ url =uri("https://maven.aliyun.com/nexus/content/groups/public/")}
}
dependencies{
    implementation("com.baidu.aip:java-sdk:4.16.2")
    implementation("com.google.code.gson:gson:2.8.8")
}