import playwright.browser.RemoteBrowser

fun main(args: Array<String>) {
    val browser = RemoteBrowser.connectWs("ws://127.0.0.1:4444/8cd249d60138f0cbda6895b869b2d25c")
    browser.newPage()
}