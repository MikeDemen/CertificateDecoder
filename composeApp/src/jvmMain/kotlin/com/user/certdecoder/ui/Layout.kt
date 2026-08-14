package com.user.certdecoder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.user.certdecoder.ui.components.ExportButtons
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.user.certdecoder.ui.utils.CertificateValidationResult
import com.user.certdecoder.ui.utils.decodeCertificate
import com.user.certdecoder.ui.utils.validateCertificate
import com.user.certdecoder.ui.utils.defaultExportFileName
import com.user.certdecoder.ui.utils.lastUsedExportDirectory
import com.user.certdecoder.ui.utils.rememberExportDirectory
import com.user.certdecoder.ui.utils.textReportBytes
import com.user.certdecoder.ui.utils.xlsxReportBytes
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch


@Composable
fun MainLayout() {

    var pemText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var isFileLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun loadCertificateFile(filePath: String, readBytes: suspend () -> ByteArray) {
        coroutineScope.launch {
            isFileLoading = true
            try {
                val bytes = readBytes()
                pemText = String(bytes, Charsets.UTF_8).trim()
                selectedFilePath = filePath
            } catch (e: Exception) {
                println("Failed to read file: $e")
            } finally {
                isFileLoading = false
            }
        }
    }

    // True from the moment an export button is clicked until the native dialog reports back —
    // guards against a second click re-opening the picker for the same export while it's still open.
    var isExportTxtPickerOpen by remember { mutableStateOf(false) }
    var isExportXlsxPickerOpen by remember { mutableStateOf(false) }

    val exportTxtLauncher = rememberFileSaverLauncher { destination ->
        isExportTxtPickerOpen = false
        destination?.let {
            try {
                it.file.writeBytes(textReportBytes(outputText))
                rememberExportDirectory(it.file)
            } catch (e: Exception) {
                println("Failed to export .txt: $e")
            }
        }
    }

    val exportXlsxLauncher = rememberFileSaverLauncher { destination ->
        isExportXlsxPickerOpen = false
        destination?.let {
            try {
                it.file.writeBytes(xlsxReportBytes(outputText))
                rememberExportDirectory(it.file)
            } catch (e: Exception) {
                println("Failed to export .xlsx: $e")
            }
        }
    }

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
            FileBrowser(
                modifier = Modifier.fillMaxWidth(),
                selectedFilePath = selectedFilePath,
                isLoading = isFileLoading,
                onFileSelected = { filePath, readBytes -> loadCertificateFile(filePath, readBytes) }
            )

            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)) {
                InputTextField(
                    text = pemText,
                    onTextChange = { pemText = it },
                    onFileSelected = { filePath, readBytes -> loadCertificateFile(filePath, readBytes) },
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
                },

                onClear = {
                    if (!pemText.isBlank() || !outputText.isBlank()) {
                        pemText = ""
                        outputText = ""
                    } else {}
                },

               onValidate = {
                   outputText = when (val result = validateCertificate(pemText)) {
                       is CertificateValidationResult.Valid ->
                           "Certificate structure is valid ✓"

                       is CertificateValidationResult.ValidWithWarning ->
                           "⚠️ ${result.message}\n\nYou can still decode it, but consider removing the prefix text."

                       is CertificateValidationResult.Invalid ->
                           "Validation failed:\n${result.reason}"
                   }
               }
           )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)) {
                OutputTextField(
                    text = outputText,
                    modifier = Modifier.fillMaxSize()
                )
            }

            ExportButtons(
                enabled = outputText.isNotBlank(),
                isExportingTxt = isExportTxtPickerOpen,
                isExportingXlsx = isExportXlsxPickerOpen,
                onExportTxt = {
                    isExportTxtPickerOpen = true
                    exportTxtLauncher.launch(
                        suggestedName = defaultExportFileName(),
                        extension = "txt",
                        directory = PlatformFile(lastUsedExportDirectory())
                    )
                },
                onExportXlsx = {
                    isExportXlsxPickerOpen = true
                    exportXlsxLauncher.launch(
                        suggestedName = defaultExportFileName(),
                        extension = "xlsx",
                        directory = PlatformFile(lastUsedExportDirectory())
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}



