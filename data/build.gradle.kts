plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":perms"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // MySQL
    runtimeOnly("com.mysql:mysql-connector-j")

    // H2 (for testing)
    testRuntimeOnly("com.h2database:h2")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test")
}
