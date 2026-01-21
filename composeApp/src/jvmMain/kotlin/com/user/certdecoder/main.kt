package com.user.certdecoder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.material.*


fun main() = application {
    var secondWindowOpened by remember { mutableStateOf(true) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Certificate Decoder",
    ) {
        App()

    }
}