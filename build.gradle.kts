import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.owasp.dependencycheck.reporting.ReportGenerator.Format.HTML
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("org.springframework.boot") version "2.7.10"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.21"
    kotlin("kapt") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
    kotlin("plugin.jpa") version "1.7.21"
    kotlin("plugin.allopen") version "1.7.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version "11.0.0"
    id("org.openapi.generator") version "6.2.1"
    id("org.owasp.dependencycheck") version "8.1.2"
}

group = "uk.gov.dluhc"
version = "latest"
java.sourceCompatibility = JavaVersion.VERSION_17

ext["snakeyaml.version"] = "1.33"
extra["springCloudVersion"] = "2.4.2"
ext["spring-security.version"] =
    "5.7.5" // Fixed CVE-2022-31690 and CVE-2022-31692 - https://spring.io/blog/2022/10/31/cve-2022-31690-privilege-escalation-in-spring-security-oauth2-client
extra["awsSdkVersion"] = "2.18.9"
extra["cucumberVersion"] = "7.11.1"
extra["junitJupiterVersion"] = "5.8.2"

allOpen {
    annotations("javax.persistence.Entity", "javax.persistence.MappedSuperclass", "javax.persistence.Embedabble")
}

repositories {
    mavenCentral()
}

apply(plugin = "org.jlleitschuh.gradle.ktlint")
apply(plugin = "org.openapi.generator")
apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")
apply(plugin = "org.jetbrains.kotlin.jvm")
apply(plugin = "org.jetbrains.kotlin.plugin.spring")
apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
apply(plugin = "org.jetbrains.kotlin.plugin.allopen")

dependencies {
    // framework
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    // api
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.13")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // webclient
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty-http")

    // spring security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation("io.awspring.cloud:spring-cloud-starter-aws-messaging")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.liquibase:liquibase-core")

    // AWS dependencies (that are defined in the BOM io.awspring.cloud:spring-cloud-aws-dependencies)
    implementation("com.amazonaws:aws-java-sdk-sts")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("software.aws.rds:aws-mysql-jdbc:1.1.4")
    runtimeOnly("software.amazon.awssdk:rds")

    // Schedulling
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.43.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.43.0")

    // AWS signer using SDK V2 library is available at https://mvnrepository.com/artifact/io.github.acm19/aws-request-signing-apache-interceptor/2.1.1
    implementation("io.github.acm19:aws-request-signing-apache-interceptor:2.1.1")

    // Test implementations
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")

    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.testcontainers:mysql:1.17.6")

    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
    testImplementation("net.datafaker:datafaker:1.6.0")

    // caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // sts
    implementation("software.amazon.awssdk:sts")

    // AWS library to support tests
    testImplementation("software.amazon.awssdk:auth")
    // Libraries to support creating JWTs in tests
    testImplementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    testImplementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Cucumber support
    testImplementation("io.cucumber:cucumber-java8")
    testImplementation("io.cucumber:cucumber-junit-platform-engine")
    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation("io.cucumber:cucumber-spring")

    // Logging
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.3")
}

dependencyManagement {
    imports {
        mavenBom("io.awspring.cloud:spring-cloud-aws-dependencies:${property("springCloudVersion")}")
        mavenBom("software.amazon.awssdk:bom:${property("awsSdkVersion")}")
        mavenBom("io.cucumber:cucumber-bom:${property("cucumberVersion")}")
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
        )
    )
}

tasks.create("generate-models-from-openapi-document-EMSIntegrationAPIs.yaml", GenerateTask::class) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/EMSIntegrationAPIs.yaml")
    packageName.set("uk.gov.dluhc.emsintegrationapi")
}
// Postal SQS Message
tasks.create(
    "generate-models-from-openapi-document-postal-vote-application-sqs-messaging.yaml",
    GenerateTask::class
) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/sqs/postal-vote-application-sqs-messaging.yaml")
    packageName.set("uk.gov.dluhc.emsintegrationapi.messaging")
}

tasks.create(
    "generate-models-from-openapi-document-proxy-vote-application-sqs-messaging.yaml",
    GenerateTask::class
) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/sqs/proxy-vote-application-sqs-messaging.yaml")
    packageName.set("uk.gov.dluhc.emsintegrationapi.messaging")
}

tasks.create("api-generate IERApi model", GenerateTask::class) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/external/ier/reference/IER-EROP-APIs.yaml")
    packageName.set("uk.gov.dluhc.external.ier")
}

tasks.create("api-generate EROManagementApi model", GenerateTask::class) {
    enabled = true
    inputSpec.set("$projectDir/src/main/resources/openapi/external/EROManagementAPIs.yaml")
    packageName.set("uk.gov.dluhc.eromanagementapi")
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
    environment = mapOf("BP_HEALTH_CHECKER_ENABLED" to "true")
    buildpacks = listOf(
        "urn:cnb:builder:paketo-buildpacks/java",
        "gcr.io/paketo-buildpacks/health-checker",
    )
}

// Exclude generated code from linting
ktlint {
    filter {
        exclude { projectDir.toURI().relativize(it.file.toURI()).path.contains("/generated/") }
    }
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
