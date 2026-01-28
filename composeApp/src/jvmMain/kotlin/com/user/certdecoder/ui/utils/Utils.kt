package com.user.certdecoder.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*


//Dialog for FileBrowser
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

// Decoder Logic
fun decodeCertificate(pem: String): String {
    val cleaned = pem.trim()
    if (cleaned.isBlank()) return "No input provided. Please paste PEM content."

    val builder = StringBuilder()

    try {
        val inputStream = ByteArrayInputStream(cleaned.toByteArray(Charsets.UTF_8))
        val certFactory = CertificateFactory.getInstance("X.509")

        // support for multiple certs in PEM
        val certs = certFactory.generateCertificates(inputStream)

        if (certs.isEmpty()) {
            return "No valid certificates found in the input."
        }

        builder.appendLine("Found ${certs.size} certificate(s) in the chain:\n")

        certs.forEachIndexed { index, cert ->
            val x509 = cert as? X509Certificate
                ?: run {
                    builder.appendLine("Certificate #$index: Not an X.509 certificate")
                    return@forEachIndexed
                }

            builder.appendLine("Certificate #${index + 1} (${if (index == 0) "Leaf/End-Entity" else if (index == certs.size - 1) "Root" else "Intermediate"}):")
            builder.appendLine("  Version: ${x509.version}")
            builder.appendLine("  Serial Number: ${x509.serialNumber.toString(16).uppercase()}")
            builder.appendLine("  Signature Algorithm: ${x509.sigAlgName}")
            builder.appendLine("  Issuer: ${x509.issuerX500Principal.name}")
            builder.appendLine("  Subject: ${x509.subjectX500Principal.name}")
            builder.appendLine("  Valid From: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault()).format(x509.notBefore)}")
            builder.appendLine("  Valid To:   ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault()).format(x509.notAfter)}")
            builder.appendLine("  Public Key Algorithm: ${x509.publicKey.algorithm}")

            // Optional: show basic key info (hex snippet)
            val pubKeyHex = x509.publicKey.encoded.joinToString("") { "%02x".format(it) }.take(64) + "..."
            builder.appendLine("  Public Key (hex prefix): $pubKeyHex")

            // Basic extensions (expandable)
            val criticalOIDs = x509.criticalExtensionOIDs ?: emptySet()
            if (criticalOIDs.isNotEmpty()) {
                builder.appendLine("  Critical Extensions:")
                criticalOIDs.forEach { oid ->
                    val value = x509.getExtensionValue(oid)
                    if (value != null) {
                        val hex = value.joinToString(" ") { "%02x".format(it) }.take(50) + "..."
                        builder.appendLine("    OID $oid: $hex")
                    }
                }
            }

            builder.appendLine("  ---")  // separator between certs
            builder.appendLine()
        }

    } catch (e: Exception) {
        builder.appendLine("Error parsing certificate chain:")
        builder.appendLine("  ${e.message ?: e::class.simpleName}")
        e.printStackTrace()  // for console debugging
    }

    return builder.toString()
}