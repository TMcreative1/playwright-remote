
repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    "testImplementation"(kotlin("test-junit5"))
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.0.0")
    "implementation"("com.squareup.okhttp3:okhttp:4.9.1")
    "testImplementation"("org.java-websocket:Java-WebSocket:1.5.2")
    "implementation"("com.google.code.gson:gson:2.8.6")
}