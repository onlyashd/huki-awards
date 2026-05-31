package io.github.onlyashd.hukiawards.util

expect fun downloadFile(
    bytes: ByteArray,
    fileName: String,
    mimeType: String = "application/octet-stream"
)
expect fun copyToClipboard(text: String)
expect fun getOrigin(): String
