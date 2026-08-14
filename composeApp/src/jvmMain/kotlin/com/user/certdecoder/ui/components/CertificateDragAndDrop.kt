package com.user.certdecoder.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import java.io.File
import java.net.URI

val SUPPORTED_CERTIFICATE_EXTENSIONS = setOf("pem", "cer", "crt")

/**
 * A [DragAndDropTarget] that accepts a dropped file with a supported certificate extension,
 * reporting hover state via [onHoverChange] (for highlighting the drop target) and the dropped
 * file via [onFileDropped]. Drops of unsupported file types are rejected.
 */
@OptIn(ExperimentalComposeUiApi::class)
internal fun createCertificateDropTarget(
    onHoverChange: (Boolean) -> Unit,
    onFileDropped: (File) -> Unit
): DragAndDropTarget = object : DragAndDropTarget {
    override fun onStarted(event: DragAndDropEvent) {
        onHoverChange(true)
    }

    override fun onEnded(event: DragAndDropEvent) {
        onHoverChange(false)
    }

    override fun onDrop(event: DragAndDropEvent): Boolean {
        val droppedFile = (event.dragData() as? DragData.FilesList)
            ?.readFiles()
            ?.firstOrNull()
            ?.let { File(URI(it)) }
            ?: return false

        if (droppedFile.extension.lowercase() !in SUPPORTED_CERTIFICATE_EXTENSIONS) {
            return false
        }

        onFileDropped(droppedFile)
        return true
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberCertificateDropTarget(
    onHoverChange: (Boolean) -> Unit,
    onFileDropped: (File) -> Unit
): DragAndDropTarget = remember { createCertificateDropTarget(onHoverChange, onFileDropped) }
