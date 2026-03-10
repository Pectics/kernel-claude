plugins {
    java
    id("org.springframework.boot") version "4.0.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "me.pectics"
version = "0.0.1-SNAPSHOT"
description = "kernel-claude - A multi-platform intelligent agent system"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// 所有子项目的通用配置
subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    // 通用依赖
    dependencies {
        // Lombok
        "compileOnly"("org.projectlombok:lombok")
        "annotationProcessor"("org.projectlombok:lombok")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
