dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":core"))

    implementation("com.google.guava:guava:33.5.0-jre")
}
