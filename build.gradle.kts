import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.2"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.20"
	kotlin("plugin.spring") version "1.5.20"
}

group = "no.iktdev"
version = "0.0.2-SNAPSHOT"
base.archivesBaseName = "streamitApi"
java.sourceCompatibility = JavaVersion.VERSION_11



repositories {
	maven {
		url = uri("https://repo.panda-lang.org/releases")
	}
	mavenCentral()
}

val exposedVersion = "0.31.1"
val swaggerVersion = "2.7.0"
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(kotlin("script-runtime"))

	implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
	implementation("net.dzikoysk:exposed-upsert:1.0.0")
	implementation ("mysql:mysql-connector-java:8.0.19")

	implementation("io.springfox:springfox-swagger2:$swaggerVersion")
	implementation("io.springfox:springfox-swagger-ui:$swaggerVersion")

}


tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
