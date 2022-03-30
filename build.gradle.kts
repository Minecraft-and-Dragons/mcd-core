plugins {
    `java-library`
    `maven-publish`
    id("io.freefair.lombok") version "6.4.1"
}

group = "com.github.mcd.core"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    api("com.github.Minestom:Minestom:244f8785ff")

    implementation("org.reflections:reflections:0.10.2")

    api("org.jetbrains:annotations:23.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.mct"
            artifactId = "core"
            version = "1.0"

            from(components["java"])
        }
    }
}