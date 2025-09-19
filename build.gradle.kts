import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.owasp.dependencycheck.reporting.ReportGenerator.Format.HTML
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import java.lang.ProcessBuilder.Redirect

plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("plugin.allopen") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version "11.0.0"
    id("org.openapi.generator") version "7.0.1"
    id("org.owasp.dependencycheck") version "12.1.3"
    id("org.liquibase.gradle") version "2.0.4"
}

group = "uk.gov.dluhc"
version = "latest"
java.sourceCompatibility = JavaVersion.VERSION_17

extra["awsSdkVersion"] = "2.26.20"
extra["springCloudVersion"] = "3.2.1"
extra["springCloudAwsVersion"] = "3.2.1"
extra["junitJupiterVersion"] = "5.10.5"

allOpen {
    annotations("jakarta.persistence.Entity", "jakarta.persistence.MappedSuperclass", "jakarta.persistence.Embedabble")
}

val awsProfile = System.getenv("AWS_PROFILE_ARG") ?: "--profile code-artifact"
val codeArtifactToken = "aws codeartifact get-authorization-token --domain erop-artifacts --domain-owner 063998039290 --query authorizationToken --output text $awsProfile".runCommand()

repositories {
    mavenCentral()
    maven {
        url = uri("https://erop-artifacts-063998039290.d.codeartifact.eu-west-2.amazonaws.com/maven/api-repo/")
        credentials {
            username = "aws"
            password = codeArtifactToken
        }
    }
}

apply(plugin = "org.jlleitschuh.gradle.ktlint")
apply(plugin = "org.openapi.generator")
apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")
apply(plugin = "org.jetbrains.kotlin.jvm")
apply(plugin = "org.jetbrains.kotlin.plugin.spring")
apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
apply(plugin = "org.jetbrains.kotlin.plugin.allopen")
apply(plugin = "org.liquibase.gradle")

liquibase {
    activities.register("main")
    runList = "main"
}

dependencies {
    // framework
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")

    // internal libs
    implementation("uk.gov.dluhc:logging-library:3.0.3")
    implementation("uk.gov.dluhc:messaging-support-library:2.2.0")
    implementation("uk.gov.dluhc:email-client:1.0.0")

    // api
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.15")
    implementation("org.webjars:swagger-ui:4.19.1")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.7")
    implementation("org.springframework:spring-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Logging
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.3")

    // spring security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("com.nimbusds:nimbus-jose-jwt:10.0.2")

    // jpa/liquibase
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.liquibase:liquibase-core")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("org.hibernate.orm:hibernate-envers")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("software.aws.rds:aws-mysql-jdbc:1.1.10")
    runtimeOnly("software.amazon.awssdk:rds")

    // AWS dependencies (that are defined in the BOM "software.amazon.awssdk")
    implementation("software.amazon.awssdk:sts")
    // email
    implementation("software.amazon.awssdk:ses")

    // messaging
    implementation("org.springframework:spring-messaging")
    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:${property("springCloudAwsVersion")}"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sns")

    implementation("io.github.acm19:aws-request-signing-apache-interceptor:3.0.0")
    implementation("org.apache.httpcomponents.client5:httpclient5")

    // caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Scheduling
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.43.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.43.0")

    // tests
    testImplementation("software.amazon.awssdk:sqs") // required to send messages to a queue, which we only need to do in test at the moment
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("net.datafaker:datafaker:1.7.0")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    testImplementation("org.testcontainers:testcontainers:1.19.8")
    testImplementation("org.testcontainers:mysql:1.19.8")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")

    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.testcontainers:mysql:1.17.6")

    testImplementation("org.wiremock:wiremock-jetty12:3.9.2")
    testImplementation("net.datafaker:datafaker:1.6.0")

    // caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // AWS library to support tests
    testImplementation("software.amazon.awssdk:auth")
    // Libraries to support creating JWTs in tests
    testImplementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    testImplementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Logging
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.3")
    // Liquibase plugin for local development
    val liquibaseRuntime by configurations
    liquibaseRuntime("org.liquibase:liquibase-core")
    liquibaseRuntime("mysql:mysql-connector-java")
    liquibaseRuntime("org.springframework.boot:spring-boot")
    liquibaseRuntime("info.picocli:picocli:4.6.1")
    liquibaseRuntime("javax.xml.bind:jaxb-api:2.3.1")
    liquibaseRuntime("org.apache.commons:commons-lang3:3.18.0")
}

dependencyManagement {
    imports {
        mavenBom("io.awspring.cloud:spring-cloud-aws-dependencies:${property("springCloudVersion")}")
        mavenBom("software.amazon.awssdk:bom:${property("awsSdkVersion")}")
        mavenBom("org.junit:junit-bom:${property("junitJupiterVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    dependsOn(tasks.withType<GenerateTask>())
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    dependsOn(tasks.withType<GenerateTask>())
    useJUnitPlatform()
    jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
}

tasks.withType<GenerateTask> {
    enabled = false
    validateSpec.set(true)
    outputDir.set("$projectDir/build/generated")
    generatorName.set("kotlin-spring")
    generateModelTests.set(false)
    generateModelDocumentation.set(false)
    globalProperties.set(
        mapOf(
            "apis" to "false",
            "invokers" to "false",
            "models" to "",
        )
    )
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "serializationLibrary" to "jackson",
            "enumPropertyNaming" to "UPPERCASE",
            "useBeanValidation" to "true",
            "useSpringBoot3" to "true",
        )
    )
}

// Register Checker API generations

tasks.create("api-generate RegisterCheckApi model", GenerateTask::class) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/registerchecker/RegisterCheckerAPIs.yaml")
    packageName.set("uk.gov.dluhc.registercheckerapi")
    configOptions.put("documentationProvider", "none")
}

tasks.create("api-generate IERApi model", GenerateTask::class) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/external/ier/reference/IER-EROP-APIs.yaml")
    packageName.set("uk.gov.dluhc.external.ier")
}

tasks.create("api-generate rca-sqs-messaging model", GenerateTask::class) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/registerchecker/sqs/RegisterSqsMessaging.yaml")
    packageName.set("uk.gov.dluhc.registercheckerapi.messaging")
}

// EMS integration API generations

tasks.create("generate-models-from-openapi-document-EMSIntegrationAPIs.yaml", GenerateTask::class) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/ems/EMSIntegrationAPIs.yaml")
    packageName.set("uk.gov.dluhc.emsintegrationapi")
}
// Postal SQS Message
tasks.create(
    "generate-models-from-openapi-document-postal-vote-application-sqs-messaging.yaml",
    GenerateTask::class
) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/ems/sqs/postal-vote-application-sqs-messaging.yaml")
    packageName.set("uk.gov.dluhc.emsintegrationapi.messaging")
}

tasks.create(
    "generate-models-from-openapi-document-proxy-vote-application-sqs-messaging.yaml",
    GenerateTask::class
) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/ems/sqs/proxy-vote-application-sqs-messaging.yaml")
    packageName.set("uk.gov.dluhc.emsintegrationapi.messaging")
}

// TODO remove this once differences have been reconciled with RC version of dependency
tasks.create("api-generate IERApi model for EMS", GenerateTask::class) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/ems/external/ier/reference/IER-EROP-APIs.yaml")
    packageName.set("uk.gov.dluhc.external.ier.ems")
}

tasks.create("api-generate EROManagementApi model", GenerateTask::class) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/external/EROManagementAPIs.yaml")
    packageName.set("uk.gov.dluhc.eromanagementapi")
}

tasks.create(
    "generate-models-from-remove-application-ems-integration-data-sqs-messaging.yaml",
    GenerateTask::class
) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/ems/sqs/remove-application-ems-integration-data-sqs-messaging.yaml")
    packageName.set("uk.gov.dluhc.emsintegrationapi.messaging")
}

// Add the generated code to the source sets
sourceSets["main"].java {
    this.srcDir("$projectDir/build/generated")
}

// Linting is dependent on GenerateTask
tasks.withType<KtLintCheckTask> {
    dependsOn(tasks.withType<GenerateTask>())
}

tasks.withType<BootBuildImage> {
    builder.set("paketobuildpacks/builder-jammy-base")
    environment.set(mapOf("BP_HEALTH_CHECKER_ENABLED" to "true"))
    buildpacks.set(
        listOf(
            "urn:cnb:builder:paketo-buildpacks/java",
            "docker.io/paketobuildpacks/health-checker",
        )
    )
}

// Exclude generated code from linting
ktlint {
    filter {
        exclude { projectDir.toURI().relativize(it.file.toURI()).path.contains("/generated/") }
    }
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
        arg("mapstruct.unmappedTargetPolicy", "IGNORE")
    }
    correctErrorTypes = true
}

fun String.runCommand(): String {
    val parts = this.split("\\s".toRegex())
    val process = ProcessBuilder(*parts.toTypedArray())
        .redirectOutput(Redirect.PIPE)
        .start()
    process.waitFor()
    return process.inputStream.bufferedReader().readText().trim()
}

/* Configuration for the OWASP dependency check */
dependencyCheck {
    autoUpdate = true
    failOnError = true
    failBuildOnCVSS = 0.toFloat()
    analyzers.assemblyEnabled = false
    analyzers.centralEnabled = true
    format = HTML.name
    suppressionFiles = listOf("owasp.suppressions.xml")
}
