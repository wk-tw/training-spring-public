java.sourceCompatibility = JavaVersion.VERSION_17

plugins {
    java
    id("org.springframework.boot") version "3.0.2" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    id("com.diffplug.spotless") version "6.15.0"
}

repositories {
    mavenCentral()
}

val junitVersion = "5.9.2"
val assertJVersion = "3.24.2"

subprojects {
    group = "com.wck"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    apply {
        plugin("java")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("com.diffplug.spotless")
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    dependencies {
        /**
         * Lombok dependencies
         */
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        /**
         * JUnit 5 dependencies
         */
        testImplementation("org.assertj:assertj-core:$assertJVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testCompileOnly("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

        /**
         * Spring Boot Test
         */
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    spotless {
        java {
            googleJavaFormat("1.15.0")
        }
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
    }
}

