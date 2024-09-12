plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jetty:jetty-server:11.0.22")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.22")
    implementation("org.glassfish.jersey.containers:jersey-container-servlet-core:3.1.2")
    implementation("org.glassfish.jersey.inject:jersey-hk2:3.1.2")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.glassfish.jaxb:jaxb-runtime:3.0.2")
    implementation("com.sun.activation:jakarta.activation:2.0.1")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.5")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}