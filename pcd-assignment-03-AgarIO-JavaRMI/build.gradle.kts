plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.create("Run-Server-and-Client") {
    doLast {
        val serverProcess = ProcessBuilder("java", "-cp", "build/classes/java/main", "it.unibo.agar.RunServerSide")
            .inheritIO()
            .start()

        // Give the server a moment to start
        Thread.sleep(2000)

        val clientProcess = ProcessBuilder("java", "-cp", "build/classes/java/main", "it.unibo.agar.RunClientSide")
            .inheritIO()
            .start()

    }
}

tasks.test {
    useJUnitPlatform()
}