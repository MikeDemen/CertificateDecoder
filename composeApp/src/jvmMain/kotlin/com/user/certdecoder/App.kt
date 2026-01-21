package com.user.certdecoder

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FilePickerContent()
        }
    }
}

@Composable
private fun FilePickerContent() {
    var showFilePicker by remember { mutableStateOf(false) }
    var selectedPath by remember { mutableStateOf<String?>(null) }

    //Column for the filePicker (in-window location)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {


        // Placeholder for decoded output (later)
        Card(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(75.dp)
                .align(Alignment.CenterHorizontally),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = selectedPath ?: "Select a certificate",
                    style = MaterialTheme.typography.bodyLarge,
                    // Memo:
                    // if selectedPath != null, MaterialTheme.colorScheme.onSurface.
                    // else MaterialTheme.colorScheme.onSurfaceVariant
                    color = selectedPath?.let {MaterialTheme.colorScheme.onSurface}
                        ?: MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        showFilePicker = true
                    }
                ) {
                    Text("Browse")
                }
            }

        }
        FilePickerDialog(
            visible = showFilePicker,
            onFileSelected = { path ->
                selectedPath = path
                showFilePicker = false

            }
        )
    }
}

@Composable
private fun FilePickerDialog(
    visible: Boolean,
    onFileSelected: (String?) -> Unit
) {
    if (visible) {
        AwtWindow(
            create = {
                object: FileDialog(null as Frame?, "Select a certificate file", FileDialog.LOAD){
                    override fun setVisible(visible: Boolean) {
                        super.setVisible(visible)
                        if (!visible) {
                            val selected = if (file != null && directory != null) {
                                File(directory, file).absolutePath
                            } else {
                                null
                            }
                            onFileSelected(selected)
                        }
                    }
                }
            },
            dispose = { it.dispose() }
        )
    }
}