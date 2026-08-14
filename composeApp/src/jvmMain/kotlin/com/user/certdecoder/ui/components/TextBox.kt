package com.user.certdecoder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.font.FontWeight
import java.io.File


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputTextField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onFileSelected: (filePath: String, readBytes: suspend () -> ByteArray) -> Unit = { _, _ -> }
){
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scrollState: ScrollState = rememberScrollState()
    var isDragHovering by remember { mutableStateOf(false) }

    val dragAndDropTarget = rememberCertificateDropTarget(
        onHoverChange = { isDragHovering = it },
        onFileDropped = { file: File -> onFileSelected(file.absolutePath) { file.readBytes() } }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dragAndDropTarget
            )
            .then(
                if (isDragHovering) {
                    Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary))
                } else {
                    Modifier
                }
            )
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                // Hide placeholder if window is selected or text is present
                when {
                    isFocused || text.isNotEmpty() -> {}
                    else                           -> Text("Paste your certificate contents here, or drop a certificate file")
                }
            },
            interactionSource = interactionSource,

            // Change color based on textField's focus state
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.Unspecified,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),

            maxLines = Int.MAX_VALUE,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(8.dp)


        )
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
fun OutputTextField(
    text: String,
    modifier: Modifier = Modifier) {

    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(12.dp)
            ) {
                if (text.isBlank()) {
                    Text(
                        "Decoded certificate details will appear here...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    text.lines().forEach { line ->
                        if (line.isBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        } else if (line.contains(":")) {
                            val (label, value) = line.split(":", limit = 2).map { it.trim() }
                            Row {
                                Text(
                                    text = "$label: ",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            // For lines without :, e.g. separators or headers
                            Text(
                                text = line,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}