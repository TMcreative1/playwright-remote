import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

tasks {
    named<Test>("test") {
        outputs.upToDateWhen { false }
        testLogging {
            events(FAILED, PASSED, SKIPPED, STANDARD_OUT)
            exceptionFormat = FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true

            debug {
                events(
                    STARTED,
                    FAILED, PASSED, SKIPPED, STANDARD_ERROR, STANDARD_OUT
                )
                exceptionFormat = FULL
            }
            info.events = debug.events
            info.exceptionFormat = debug.exceptionFormat
        }
        useJUnitPlatform {
            include("**/Test*.class")
        }
        dependsOn("downloadAndUnzip")
        systemProperty("browser", System.getProperty("browser"))
        maxHeapSize = "4096m"
    }
}