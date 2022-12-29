import com.avast.gradle.dockercompose.ComposeExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.7.3" apply (true)
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    id("java")
    id("com.avast.gradle.docker-compose") version "0.16.9"
    id("io.franzbecker.gradle-lombok") version "4.0.0"
    kotlin("jvm") version "1.8.0-RC2"
    kotlin("plugin.spring") version "1.6.21"

    // Less restrictive local docker authentication works.
    // id("com.google.cloud.tools.jib") version "2.7.1"

    // Stricter local docker authentication causes issues.
    // id("com.google.cloud.tools.jib") version "3.3.0"

    // id("org.eclipse.jkube.kubernetes") version "1.9.1"
}

extra["springCloudVersion"] = "2021.0.4"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

tasks {
    getByName<BootJar>("bootJar") { enabled = false }

    getByName<Jar>("jar") { enabled = false }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "19"
        }
    }

    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(
            listOf(
                "--enable-preview",
                "--add-modules",
                "jdk.incubator.concurrent"
            )
        )
        targetCompatibility = "19"
    }

    withType<Test>().configureEach {
        jvmArgs =
            listOf(
                "--enable-preview",
                "--add-modules",
                "jdk.incubator.concurrent"
            )
    }
}

dockerCompose {
    useComposeFiles.set(listOf("${projectDir}/docker-compose.yml"))
    buildBeforeUp.set(false)
    useComposeFiles.set(listOf("docker/docker-compose.yml"))
}

configure<ComposeExtension> {
    includeDependencies.set(true)
    createNested("local").apply {
        setProjectName("foo")
        environment.putAll(mapOf("TAGS" to "feature-test,local"))
        startedServices.set(listOf("foo-api", "foo-integration"))
        upAdditionalArgs.set(listOf("--no-deps"))
    }
}

subprojects {
    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
    }

    // All Java/Kotlin versions are defined here.
    java.sourceCompatibility = JavaVersion.VERSION_19
    java.targetCompatibility = JavaVersion.VERSION_19

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "19"
        }
    }

    group = "edu.vandy.recommender"
    version = "1.0.0"

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dependencyManagement {
        imports {
            mavenBom(
                "org.springframework.cloud:spring-cloud-dependencies:${
                    property(
                        "springCloudVersion"
                    )
                }"
            )
        }
    }

    dependencies {
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        testImplementation("junit:junit")
        testImplementation("org.assertj:assertj-core:3.23.1")
        testImplementation("io.projectreactor:reactor-test")

        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("org.springframework.boot:spring-boot-starter-parent:2.7.3")
        developmentOnly("org.springframework.boot:spring-boot-devtools")
        implementation("org.springframework:spring-test:5.3.22")
        implementation("org.springframework.boot:spring-boot-test-autoconfigure:2.7.3")
        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org-mockito")
        }
        testImplementation("com.ninja-squad:springmockk:3.1.1")
    }

    configure<SourceSetContainer> {
        named("main") {
            java.srcDir("src/main/java")
        }
    }
}

//if (file("$projectDir/admin/skeleton.gradle").isFile) {
//    apply(from = "$projectDir/admin/skeleton.gradle")
//}

