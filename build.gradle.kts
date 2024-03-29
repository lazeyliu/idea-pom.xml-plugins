plugins {
    java apply true
    id("org.jetbrains.intellij") version "1.17.2" apply true
}

group = "com.github.rxyor.plugin.pom.assistant"
version = "2.1"

repositories {

    mavenCentral()

}

dependencies {
    implementation("com.google.guava:guava:33.1.0-jre")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.dom4j:dom4j:2.1.4")
    implementation("org.jsoup:jsoup:1.17.2")

    annotationProcessor("org.projectlombok:lombok:1.18.32")
    compileOnly("org.projectlombok:lombok:1.18.32")

    testImplementation("junit:junit:4.13.2")
}

intellij {
    version.set("2024.1")
    type.set("IC")
    plugins.set(listOf("maven"))
    updateSinceUntilBuild.set(false)
    sameSinceUntilBuild.set(false)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
