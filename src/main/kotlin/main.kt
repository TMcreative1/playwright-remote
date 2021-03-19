import com.playwright.remote.playwright.browser.RemoteBrowser

fun main(args: Array<String>) {
    val browser = RemoteBrowser.connectWs("ws://127.0.0.1:4444/7dc9385fedfed927e424380d15016c3f")
    browser.newPage()
}