package com.user.certdecoder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.user.certdecoder.ui.utils.FilePickerDialog

@Composable
fun FileBrowser(modifier: Modifier = Modifier) {

    var showFilePicker by remember { mutableStateOf(false) }
    var selectedPath by remember { mutableStateOf<String?>(null) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween){

                Text(
                    // if selected path is null - display message "Select a certificate"
                    text = selectedPath ?: "Select a certificate",
                    style = MaterialTheme.typography.bodyLarge,
                    // Memo:
                    // if selectedPath != null, MaterialTheme.colorScheme.onSurface.
                    // else MaterialTheme.colorScheme.onSurfaceVariant
                    color = selectedPath?.let {MaterialTheme.colorScheme.onSurface}
                        ?: MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(onClick = { showFilePicker = true }) {
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