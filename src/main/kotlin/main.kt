import playwright.browser.RemoteBrowser

fun main(args: Array<String>) {
    val browser = RemoteBrowser.connectWs("ws://127.0.0.1:4444/c06c6b659d60048fc973044211035a4c")
}