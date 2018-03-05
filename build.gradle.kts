import groovy.lang.Closure
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val kotlinVersion = "1.2.30"
    kotlin("jvm").version(kotlinVersion)
    id("org.jetbrains.kotlin.plugin.spring").version(kotlinVersion)
    id("org.jetbrains.kotlin.plugin.allopen").version(kotlinVersion)
    id("org.springframework.boot").version("2.0.0.RELEASE").apply(false)
    id("com.palantir.git-version").version("0.10.1")
    id("io.spring.dependency-management").version("1.0.4.RELEASE")
    id("com.gorylenko.gradle-git-properties").version("1.4.2")
    id("com.github.ben-manes.versions").version("0.17.0")
}

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
val projectGitVersion: String = (project.ext["gitVersion"] as Closure<*>)() as String

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
//        plugin("com.gorylenko.gradle-git-properties")
        plugin("io.spring.dependency-management")
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_1_9
        targetCompatibility = JavaVersion.VERSION_1_9
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }
        dependencies {
            dependency("io.github.microutils:kotlin-logging:1.5.3")
        }
    }

    dependencies {
        compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        compile("org.jetbrains.kotlin:kotlin-reflect")
        compile("io.github.microutils:kotlin-logging")

        testCompile("junit:junit")
    }

    tasks {
        withType(KotlinCompile::class.java) {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }

        "generateGitProperties"().dependsOn("processResources")
    }
}

project(":ws-client") {
    apply {
        plugin("org.springframework.boot")
    }

    dependencies {
        compile(project(":ws-models"))

        compile("org.springframework.boot:spring-boot-starter-webflux")
        compile("org.springframework.boot:spring-boot-actuator")
//        compile("org.springframework.boot:spring-boot-devtools")
        compile("org.springframework.boot:spring-boot-configuration-processor")
        compile("com.fasterxml.jackson.module:jackson-module-kotlin")
        compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
        compile("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    }

    tasks {
        "bootRun"(type = JavaExec::class) {
            systemProperty("spring.output.ansi.enabled", "always")
        }
    }
}

project(":ws-server") {
    apply {
        plugin("org.springframework.boot")
    }

    dependencies {
        compile(project(":ws-models"))

        compile("org.springframework.boot:spring-boot-starter-webflux")
        compile("org.springframework.boot:spring-boot-actuator")
//        compile("org.springframework.boot:spring-boot-devtools")
        compile("org.springframework.boot:spring-boot-configuration-processor")
        compile("com.fasterxml.jackson.module:jackson-module-kotlin")
        compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
        compile("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")

        compile("net.jpountz.lz4:lz4:1.3")
        compile("de.ruedigermoeller:fst:2.57")
    }


    tasks {
        "bootRun"(type = JavaExec::class) {
            systemProperty("spring.output.ansi.enabled", "always")
        }
    }
}

tasks {
    "wrapper"(type = Wrapper::class) {
        gradleVersion = "4.6"
        distributionType = org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
    }
}
