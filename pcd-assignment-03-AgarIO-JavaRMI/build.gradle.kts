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
tasks.create("Run-Server-and-Clients") {
    doLast {
        try {
            val serverProcess = ProcessBuilder("java", "-cp", "build/classes/java/main", "it.unibo.agar.RunServerSide")
                .inheritIO()
                .start()

            // Attendi che il server si avvii
            Thread.sleep(1000)

            val clientNames = listOf("p1", "p2", "p3", "p4")

            val clientProcesses = clientNames.map { clientName ->
                try {
                    ProcessBuilder("java", "-cp", "build/classes/java/main", "it.unibo.agar.RunClientSide", clientName)
                        .inheritIO()
                        .start()
                } catch (e: Exception) {
                    println("Errore durante l'avvio del client $clientName: ${e.message}")
                    null
                }
            }.filterNotNull()

            // Opzionalmente, attendi la terminazione dei processi client
            clientProcesses.forEach { it.waitFor() }
        } catch (e: Exception) {
            println("Errore durante l'avvio del server: ${e.message}")
        }
    }
}
tasks.test {
    useJUnitPlatform()
}