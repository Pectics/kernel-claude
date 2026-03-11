plugins {
    id("org.springframework.boot")
}

dependencies {
    compileOnly(project(":perms"))

    compileOnly("org.springframework.boot:spring-boot-starter")

    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter")
    implementation("com.github.ben-manes.caffeine:caffeine")

    runtimeOnly("com.mysql:mysql-connector-j")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(project(":perms"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
