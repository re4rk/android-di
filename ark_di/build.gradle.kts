plugins {
    kotlin("jvm")
    id("kotlin-kapt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
dependencies {
    implementation("javax.inject", "javax.inject", "1")
    implementation("org.jetbrains.kotlin", "kotlin-reflect", "1.8.21")
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.8.2")
    testImplementation("org.assertj", "assertj-core", "3.22.0")

    annotationProcessor("com.google.auto.service:auto-service:1.0-rc6")
    implementation("com.google.auto.service:auto-service:1.0-rc6")
    compileOnly("com.google.auto.service:auto-service:1.0-rc6")

    kapt("com.google.auto.service:auto-service:1.0-rc6")

    // for FileSpec
    implementation("com.squareup:kotlinpoet:1.12.0")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask>()
    .configureEach {
        kaptProcessJvmArgs.add("-Xmx512m")
    }

kapt {
    correctErrorTypes = true
}
