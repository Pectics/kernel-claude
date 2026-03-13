plugins {
    java
    id("org.springframework.boot") version "4.0.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "me.pectics"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// 所有子模块的公共配置
subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    group = rootProject.group
    version = rootProject.version

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.3")
        }
        dependencies {
            dependency("org.jetbrains:annotations:26.1.0")

            dependency("org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.1")
            dependency("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:4.0.1")
        }
    }

    dependencies {
        compileOnly("org.jetbrains:annotations")
        compileOnly("org.projectlombok:lombok")

        annotationProcessor("org.projectlombok:lombok")

        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.assertj:assertj-core")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

}
