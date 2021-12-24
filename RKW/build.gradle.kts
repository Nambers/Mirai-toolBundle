plugins {
    val kotlinVersion = "1.5.32"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.8.3"
}

group = "tech.eritquearcus"
version = "1.3.1"

repositories {
    //国内镜像源
    mavenLocal()
    mavenCentral()
    maven{ url =uri("https://maven.aliyun.com/nexus/content/groups/public/")}
}
dependencies{
    implementation("com.baidu.aip:java-sdk:4.16.3")
    implementation("com.google.code.gson:gson:2.8.9")
}