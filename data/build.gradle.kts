plugins {
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":perms"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.google.guava:guava:33.5.0-jre")

    // Database drivers
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.xerial:sqlite-jdbc")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test")
    testRuntimeOnly("org.xerial:sqlite-jdbc")
    testRuntimeOnly("com.h2database:h2")
}
