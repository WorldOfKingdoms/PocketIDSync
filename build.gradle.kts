import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
    id("io.freefair.lombok") version "8.4"
}

group = "be.esmay.pocketidsync"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://nexus.velocitypowered.com/repository/maven-public/")
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    implementation("redis.clients:jedis:5.2.0")

    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    implementation("org.spongepowered:configurate-yaml:4.2.0")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Built-By"] = System.getProperty("user.name")
        attributes["Built-JDK"] = System.getProperty("java.version")
        attributes["Build-Time"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        attributes["Implementation-Version"] = version
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}