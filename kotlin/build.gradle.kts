plugins {
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    kotlin("plugin.jpa") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
}

allprojects {
    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(17)
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
            mavenBom("io.awspring.cloud:spring-cloud-aws-dependencies:${property("springCloudAwsVersion")}")
            mavenBom("io.micrometer:micrometer-tracing-bom:1.4.4")
        }
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Spring plugin / Jackson は domain 以外のモジュールにのみ適用
val springModules = subprojects.filter {
    it.path.contains("bootstrap") ||
        it.path.contains("infrastructure") ||
        it.path.contains("application") ||
        it.name == "common"
}
configure(springModules) {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    dependencies {
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
    }
}
