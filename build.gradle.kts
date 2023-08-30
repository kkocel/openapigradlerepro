import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jjwtVersion = "0.11.5"
val kotlinTestVersion = "5.6.2"
val kotlinLoggingVersion = "3.0.5"
val springdocVersion = "2.2.0"
val testContainersVersion = "1.18.3"

plugins {
    application
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.3"
    id("org.graalvm.buildtools.native") version "0.9.24"
    id("org.springdoc.openapi-gradle-plugin") version "1.7.0"
    val kotlinVersion = "1.9.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

openApi {
    outputDir.set(file("$projectDir/docs"))
    apiDocsUrl.set("http://localhost:8080/v3/api-docs.yaml")
    outputFileName.set("openapi.yaml")
}

tasks.named("build") {
    finalizedBy("generateOpenApiDocs")
}

/**
 * Unfortunately none of the tips from https://github.com/springdoc/springdoc-openapi-gradle-plugin/issues/102 seems to work.
 * I was following the error message from Gradle and define explicit dependencies on required tasks (some of them will resolve dependency on other tasks in the graph).
 * Lets see how issue 102 gets resolved.
 */
tasks {
    forkedSpringBootRun {
        dependsOn(
            project.tasks.named("distTar"),
            project.tasks.named("distZip"),
            project.tasks.named("bootDistTar"),
            project.tasks.named("bootDistZip"),
            project.tasks.named("test")
        )
    }
}

application {
    mainClass.set("com.example.openapigradlerepro.OpenapigradlereproApplicationKt")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

graalvmNative.toolchainDetection.set(true)

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:$springdocVersion")
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    testImplementation("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.kotest:kotest-runner-junit5:$kotlinTestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotlinTestVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.wrapper {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.BIN
}
