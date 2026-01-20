package com.user.certdecoder

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Certificate Decoder",
    ) {
        App()
    }
}