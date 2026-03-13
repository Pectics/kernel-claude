plugins {
    id("java-library")
}

dependencies {
    // SLF4J API
    api("org.slf4j:slf4j-api:2.0.16")

    // Jetbrains annotations
    compileOnly("org.jetbrains:annotations:24.0.1")
}
