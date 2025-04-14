plugins {
	kotlin("jvm") version "1.8.21"
	id("org.springframework.boot") version "2.5.5"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("plugin.spring") version "1.5.31"
}

group = "no.iktdev"
version = "1.0.0-SNAPSHOT"
base.archivesBaseName = "streamit.api"
java.sourceCompatibility = JavaVersion.VERSION_11



repositories {
	mavenCentral()
	maven {
		url = uri("https://reposilite.iktdev.no/releases")
	}
	maven {
		url = uri("https://reposilite.iktdev.no/snapshots")
	}
}

val exposedVersion = "0.44.0"
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-configuration-processor")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("junit:junit:4.13.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(kotlin("script-runtime"))

	implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
	implementation ("mysql:mysql-connector-java:8.0.29")

	implementation("org.springdoc:springdoc-openapi-ui:1.6.4")

	implementation ("com.google.code.gson:gson:2.9.0")

	implementation ("com.auth0:java-jwt:4.0.0")
	implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")

	implementation("com.google.firebase:firebase-admin:9.2.0")

	implementation("no.iktdev.streamit.library:streamit-library-db:1.0.0-alpha15")
	//implementation("no.iktdev.streamit.library:streamit-library-kafka:0.0.2-alpha75")

	testImplementation("junit:junit:4.13.2")
	testImplementation("org.junit.jupiter:junit-jupiter")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
	testImplementation("org.assertj:assertj-core:3.4.1")
	testImplementation("org.mockito:mockito-core:3.+")


}



tasks.test {
	useJUnitPlatform()
}



tasks.bootJar {
	archiveFileName.set("app.jar")
	launchScript()
}

tasks.jar {
	archiveFileName.set("app.jar")
	archiveBaseName.set("app")
}