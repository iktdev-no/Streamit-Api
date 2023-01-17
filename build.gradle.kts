plugins {
	id("org.springframework.boot") version "2.5.2"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.5.31"
}

group = "no.iktdev"
version = "0.0.5.2-SNAPSHOT"
base.archivesBaseName = "streamit.api"
java.sourceCompatibility = JavaVersion.VERSION_11



repositories {
	mavenCentral()
}

val exposedVersion = "0.38.2"
val swaggerVersion = "3.0.0"
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-configuration-processor")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(kotlin("script-runtime"))

	implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
	implementation ("mysql:mysql-connector-java:8.0.29")

	implementation("io.springfox:springfox-swagger2:$swaggerVersion")
	implementation("io.springfox:springfox-swagger-ui:$swaggerVersion")

	implementation ("com.google.code.gson:gson:2.9.0")

	implementation ("com.auth0:java-jwt:4.0.0")

}

tasks.withType<Test> {
	useJUnitPlatform()
}
