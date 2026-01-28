package com.user.certdecoder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.user.certdecoder.ui.components.FileBrowser
import com.user.certdecoder.ui.components.InputTextField
import com.user.certdecoder.ui.components.OutputTextField
import com.user.certdecoder.ui.components.FunctionButtons
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.user.certdecoder.ui.utils.decodeCertificate


@Composable
fun MainLayout() {

    var pemText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileBrowser(modifier = Modifier
                .fillMaxWidth()
                .weight(1f))

            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)) {
                InputTextField(
                    text = pemText,
                    onTextChange = { pemText = it },
                    modifier = Modifier.fillMaxSize()
                )
            }

            FunctionButtons(
                onDecode = {
                    outputText = if (pemText.trim().isBlank()) {
                        "No input provided. Please paste a certificate."
                    } else {
                        try {
                            decodeCertificate(pemText.trim())
                        } catch (e: Exception) {
                            "Error: ${e.message ?: e::class.simpleName}"
                        }
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutputTextField(
                text = outputText,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}



