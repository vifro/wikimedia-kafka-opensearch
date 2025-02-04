plugins {
    kotlin("jvm") version "1.8.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.kafka", "kafka-clients", "3.3.1")
    implementation("org.slf4j", "slf4j-api", "1.7.16")
    implementation("org.slf4j", "slf4j-simple", "1.7.16")
    implementation("com.squareup.okhttp3", "okhttp", "4.9.3")
    implementation("com.launchdarkly", "okhttp-eventsource", "2.5.0")

    // https://mvnrepository.com/artifact/org.opensearch.client/opensearch-rest-high-level-client/1.2.4
    implementation("org.opensearch.client", "opensearch-rest-high-level-client","1.2.4")
    implementation("com.google.code.gson", "gson", "2.9.1")

    // For streams
    implementation("org.apache.kafka", "kafka-streams", "3.3.1")
    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.13.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}