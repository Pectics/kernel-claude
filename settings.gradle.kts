rootProject.name = "kernel-claude"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

// 子模块定义
include("core")   // 核心框架
include("events") // 事件系统
include("perms")  // 权限管理系统
include("api")    // 对外 API
include("app")    // Spring Boot 启动模块
