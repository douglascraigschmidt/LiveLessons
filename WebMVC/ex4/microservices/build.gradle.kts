import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.jetbrains.kotlin.util.capitalizeDecapitalize.decapitalizeAsciiOnly
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework:spring-test:5.3.22")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(project(mapOf("path" to ":gateway")))
}

val microservicesPackage = "${project.group}.microservice"
val defaultMicroservice = "$microservicesPackage.sequentialstream.SequentialStreamApplication"

/**
 * Setting a dummy mainClass for the bootJar task
 * allows the task to map to successfully invoke
 * each microservice bootJar task as dependencies.
 */
springBoot {
    mainClass.set(defaultMicroservice)
}

/**
 * Finds all microservice application class files.
 */
val applicationClasses =
    sourceSets.main.get().allJava.sourceDirectories.files
        .flatMap { file ->
            val walk: FileTreeWalk = file.walk()
            walk.filter {
                it.isFile && it.readText().contains("@SpringBootApplication")
            }.map {
                it.name.substringBefore(".")
            }
        }

tasks.create("microservice")

/**
 * Converts microservice application class names
 * to a list of base names used to create tasks
 * for each microservice application.
 */
val microservices =
    applicationClasses
        .map { it.substringBefore("Application").decapitalizeAsciiOnly() }

val microservicesGroup = "microservices"
val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeChar = "-"
val snakeRegex = "$snakeChar[a-zA-Z]".toRegex()
val classRegex = "([a-zA-Z])(.*)"

fun String.camelToSnakeCase(): String =
    replace(camelRegex) {
        "$snakeChar${it.value}"
    }.toLowerCase()

fun String.snakeToLowerCamelCase(): String =
    replace(snakeRegex) {
        it.value.replace(snakeChar, "").toLowerCaseAsciiOnly()
    }

fun String.snakeToUpperCamelCase(): String =
    replace(snakeRegex) {
        it.value.replace(snakeChar, "").toUpperCaseAsciiOnly()
    }

/**
 * For multiple microservices in one project you can't
 * have a single bootJar task since there will be many
 * boot JARs. The tasks provide individual bootJar tasks
 * for each service and a aggregation of those tasks in
 * a single bootJars task that can be used externally.
 */
tasks {
    val microservicesGroup = "microservices"

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
