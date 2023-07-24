import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.runShadow
import java.io.File

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "ru.timoxa0"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://m2.chew.pro/snapshots") }
}

dependencies {

    // https://mvnrepository.com/artifact/net.dv8tion/JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.12") {
        exclude(module = "opus-java")
    }

    // https://m2.chew.pro/#/snapshots/pw/chew/jda-chewtils/2.0-SNAPSHOT
    implementation("pw.chew:jda-chewtils:2.0-SNAPSHOT")

    // https://mvnrepository.com/artifact/io.undertow/undertow-core
    implementation("io.undertow:undertow-core:2.3.7.Final")

    // https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4")

    implementation("com.github.f4b6a3:uuid-creator:5.3.2")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-api
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-slf4j-impl
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")

    // https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:20230618")

    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation("commons-codec:commons-codec:1.16.0")

    // https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
}
application { mainClass.set("ru.timoxa0.GABot.Main") }
tasks.run.get().workingDir = File(/* pathname = */ "workdir")

tasks {
    named<Jar>("jar") {
        manifest.attributes["Multi-Release"] = "true"
    }
}


tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("GABot")
    }
}
tasks.runShadow.get().workingDir = File(/* pathname = */ "workdir")

