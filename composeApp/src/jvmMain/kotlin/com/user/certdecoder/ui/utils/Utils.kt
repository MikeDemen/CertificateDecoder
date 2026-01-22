package com.user.certdecoder.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun FilePickerDialog(
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