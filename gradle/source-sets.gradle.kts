configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
    named("test") {
        java.srcDir("src/test/kotlin")
    }
}