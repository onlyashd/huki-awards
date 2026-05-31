package io.github.onlyashd.hukiawards.util

import kotlinx.browser.document
import kotlinx.browser.window
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

actual fun downloadFile(bytes: ByteArray, fileName: String, mimeType: String) {
    val uint8Array = Uint8Array(bytes.size)
    for (i in bytes.indices) {
        uint8Array[i] = bytes[i]
    }

    val jsArray = JsArray<JsAny?>()
    jsArray.set(0, uint8Array)

    val blob = Blob(jsArray, BlobPropertyBag(type = mimeType))
    val url = URL.createObjectURL(blob)

    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = url
    anchor.download = fileName
    anchor.click()

    URL.revokeObjectURL(url)
}

actual fun copyToClipboard(text: String) {
    window.navigator.clipboard.writeText(text)
}

actual fun getOrigin(): String {
    return window.location.origin
}
