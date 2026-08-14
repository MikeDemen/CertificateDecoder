package com.user.certdecoder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FileBrowser(
    modifier: Modifier = Modifier,
    selectedFilePath: String?,
    isLoading: Boolean,
    onFileSelected: (filePath: String, readBytes: suspend () -> ByteArray) -> Unit
) {
    var isDragHovering by remember { mutableStateOf(false) }
    // True from the moment "Browse" is clicked until the native dialog reports back — guards
    // against a second click re-opening the picker while the first dialog is still opening.
    var isPickerOpen by remember { mutableStateOf(false) }

    // FileKit launcher – this is the modern picker
    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(SUPPORTED_CERTIFICATE_EXTENSIONS.toList())
    ) { platformFile ->
        isPickerOpen = false
        if (platformFile != null) {
            onFileSelected(platformFile.path) { platformFile.readBytes() }
        }
    }

    val dragAndDropTarget = rememberCertificateDropTarget(
        onHoverChange = { isDragHovering = it },
        onFileDropped = { file -> onFileSelected(file.absolutePath) { file.readBytes() } }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dragAndDropTarget
            ),
        border = if (isDragHovering) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (selectedFilePath != null) {
                SelectableFilePathText(
                    text = selectedFilePath,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )
            } else {
                Text(
                    text = " Drag or browse for a certificate",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Button(
                    onClick = {
                        isPickerOpen = true
                        launcher.launch()
                    },
                    enabled = !isPickerOpen
                ) {
                    Text("Browse")
                }
            }
        }
    }
}

// Distance from the edge of the field, in px, within which a selection drag starts auto-scrolling.
private const val AUTO_SCROLL_EDGE_PX = 40f

// How many px the field scrolls per tick for each px the pointer sits past the edge threshold.
private const val AUTO_SCROLL_SPEED_FACTOR = 1.2f

// Delay between auto-scroll ticks while dragging past an edge.
private const val AUTO_SCROLL_TICK_MS = 16L

/**
 * Displays [text] on a single line, scrolled so the end of the text (e.g. the file name at the
 * end of a path) is visible by default. The full text remains selectable/copyable even while
 * part of it is scrolled out of view, and dragging a selection towards either edge of the field
 * auto-scrolls so the rest of the text can be reached.
 */
@Composable
private fun SelectableFilePathText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(text) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    SelectionContainer(
        modifier = modifier.pointerInput(scrollState) {
            // Observed on the Initial pass without consuming, so the selection drag gesture
            // handled further down (on the Text) still sees every event as normal.
            while (true) {
                val down = awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                }
                val pointerId = down.id
                var pointerX = down.position.x

                coroutineScope {
                    val autoScrollJob = launch {
                        while (isActive) {
                            val width = size.width.toFloat()
                            val speed = when {
                                pointerX < AUTO_SCROLL_EDGE_PX ->
                                    -(AUTO_SCROLL_EDGE_PX - pointerX).coerceIn(0f, AUTO_SCROLL_EDGE_PX)
                                pointerX > width - AUTO_SCROLL_EDGE_PX ->
                                    (pointerX - (width - AUTO_SCROLL_EDGE_PX)).coerceIn(0f, AUTO_SCROLL_EDGE_PX)
                                else -> 0f
                            } * AUTO_SCROLL_SPEED_FACTOR

                            if (speed != 0f) {
                                scrollState.scrollBy(speed)
                            }
                            delay(AUTO_SCROLL_TICK_MS)
                        }
                    }

                    try {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                if (!change.pressed) break
                                pointerX = change.position.x
                            }
                        }
                    } finally {
                        autoScrollJob.cancel()
                    }
                }
            }
        }
    ) {
        Text(
            text = text,
            style = style,
            color = color,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.horizontalScroll(scrollState)
        )
    }
}
