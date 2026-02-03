package com.user.certdecoder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.launch
import java.io.File
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes


@Composable
fun FileBrowser(
    modifier: Modifier = Modifier,
    onPemLoaded: (String) -> Unit = {}
) {
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // FileKit launcher – this is the modern picker
    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(listOf("pem", "cer", "crt"))  // or PickerType.Any if you want broader
    ) { platformFile ->
        if (platformFile != null) {
            coroutineScope.launch {
                isLoading = true
                try {
                    val bytes = platformFile.readBytes()
                    val content = String(bytes, Charsets.UTF_8).trim()   // ← safe & explicit

                    onPemLoaded(content)

                    val fileName = platformFile.path
                        ?.substringAfterLast('/', "")
                        ?.substringAfterLast('\\', "")
                        ?: "Selected Certificate"

                    selectedFileName = fileName
                } catch (e: Exception) {
                    println("Failed to read file: $e")
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedFileName ?: "Select a certificate",
                style = MaterialTheme.typography.bodyLarge,
                color = selectedFileName?.let { MaterialTheme.colorScheme.onSurface }
                    ?: MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Button(onClick = { launcher.launch() }) {
                    Text("Browse")
                }
            }
        }
    }
}