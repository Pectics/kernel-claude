plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":events"))
    implementation(project(":perms"))
    implementation(project(":data"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
