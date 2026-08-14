package com.user.certdecoder.ui.components

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.geometry.Offset
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenCalled

@OptIn(ExperimentalComposeUiApi::class)
private fun dropEventFor(vararg fileUris: String): DragAndDropEvent {
    val transferable = object : Transferable {
        override fun getTransferDataFlavors() = arrayOf(DataFlavor.javaFileListFlavor)
        override fun isDataFlavorSupported(flavor: DataFlavor) = flavor == DataFlavor.javaFileListFlavor
        override fun getTransferData(flavor: DataFlavor): Any = fileUris.map { File(URI(it)) }
    }
    val nativeEvent = mock(DropTargetDropEvent::class.java)
    whenCalled(nativeEvent.transferable).thenReturn(transferable)
    return DragAndDropEvent(action = null, nativeEvent = nativeEvent, positionInRootImpl = Offset.Zero)
}

class CertificateDragAndDropTest {

    @Test
    fun onDrop_withSupportedExtension_invokesCallbackAndAccepts() {
        var droppedFile: File? = null
        val target = createCertificateDropTarget(
            onHoverChange = {},
            onFileDropped = { droppedFile = it }
        )

        val accepted = target.onDrop(dropEventFor("file:/tmp/cert.pem"))

        assertTrue(accepted)
        assertEquals(File(URI("file:/tmp/cert.pem")), droppedFile)
    }

    @Test
    fun onDrop_withUnsupportedExtension_rejectsAndDoesNotInvokeCallback() {
        var droppedFile: File? = null
        val target = createCertificateDropTarget(
            onHoverChange = {},
            onFileDropped = { droppedFile = it }
        )

        val accepted = target.onDrop(dropEventFor("file:/tmp/notes.txt"))

        assertFalse(accepted)
        assertNull(droppedFile)
    }

    @Test
    fun onStartedAndOnEnded_reportHoverState() {
        val hoverChanges = mutableListOf<Boolean>()
        val target = createCertificateDropTarget(
            onHoverChange = { hoverChanges.add(it) },
            onFileDropped = {}
        )

        target.onStarted(dropEventFor())
        target.onEnded(dropEventFor())

        assertEquals(listOf(true, false), hoverChanges)
    }
}
