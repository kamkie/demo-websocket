import groovy.lang.Closure
import org.gradle.api.internal.FeaturePreviews.Feature.withName
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val kotlinVersion = "1.3.11"
    kotlin("jvm").version(kotlinVersion)
    id("org.jetbrains.kotlin.plugin.spring").version(kotlinVersion)
    id("org.jetbrains.kotlin.plugin.allopen").version(kotlinVersion)
    id("org.springframework.boot").version("2.1.1.RELEASE").apply(false)
    id("com.palantir.git-version").version("0.12.0-rc2")
    id("com.gorylenko.gradle-git-properties").version("2.0.0-beta1")
    id("com.github.ben-manes.versions").version("0.20.0")
}

val projectGitVersion: String = (project.ext["gitVersion"] as Closure<*>)() as String
val javaVersion = JavaVersion.VERSION_11
val kotlinVersion: String? by extra {
    buildscript.configurations["classpath"]
            .resolvedConfiguration.firstLevelModuleDependencies
            .find { it.moduleName == "org.jetbrains.kotlin.jvm.gradle.plugin" }?.moduleVersion
}
project.ext["kotlin.version"] = kotlinVersion
val springBootVersion: String? by extra {
    buildscript.configurations["classpath"]
            .resolvedConfiguration.firstLevelModuleDependencies
            .find { it.moduleName == "org.springframework.boot.gradle.plugin" }?.moduleVersion
}
val kotlinLoggingVersion = "1.6.22"
val lz4Version = "1.3"
val fstVersion = "2.57"

allprojects {
    group = "net.devopssolutions"
    version = projectGitVersion

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.allopen")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("com.github.ben-manes.versions")
        plugin("com.gorylenko.gradle-git-properties")
    }
    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    dependencies {
        implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))


        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
        implementation("io.github.microutils:kotlin-logging:${kotlinLoggingVersion}")
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }

        getByName("processResources").dependsOn("generateGitProperties")
    }
}

project(":ws-client") {
    apply {
        plugin("org.springframework.boot")
    }

    dependencies {
        implementation(project(":ws-models"))

        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-actuator")
//        implementation("org.springframework.boot:spring-boot-devtools")
        implementation("org.springframework.boot:spring-boot-configuration-processor")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    }

    tasks.getByName<JavaExec>("bootRun") {
        systemProperty("spring.output.ansi.enabled", "always")
    }
}

project(":ws-server") {
    apply {
        plugin("org.springframework.boot")
    }

    dependencies {
        implementation(project(":ws-models"))

        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-actuator")
//        implementation("org.springframework.boot:spring-boot-devtools")
        implementation("org.springframework.boot:spring-boot-configuration-processor")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")

        implementation("net.jpountz.lz4:lz4:${lz4Version}")
        implementation("de.ruedigermoeller:fst:${fstVersion}")
    }

    tasks.getByName<JavaExec>("bootRun") {
        systemProperty("spring.output.ansi.enabled", "always")
    }
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "5.1-rc-1"
    distributionType = Wrapper.DistributionType.ALL
}
