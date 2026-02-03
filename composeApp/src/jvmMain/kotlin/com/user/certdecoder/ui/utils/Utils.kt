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
import java.security.cert.CertificateException
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import androidx.compose.runtime.key




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

//Cert Validation Logic
sealed class CertificateValidationResult {
    data object Valid : CertificateValidationResult()

    data class ValidWithWarning(val message: String) : CertificateValidationResult()

    data class Invalid(val reason: String) : CertificateValidationResult()
}

fun validateCertificate(pem: String): CertificateValidationResult {
    if (pem.trim().isBlank()) {
        return CertificateValidationResult.Invalid(
            "No input provided.\n" +
                    "The pasted content is empty or contains only whitespace.\n" +
                    "Please paste a valid PEM certificate (single or chain)."
        )
    }

    val rawLines = pem.lines()                      // 0-based, original for accurate line numbers
    val nonBlankLines = rawLines.filter { it.trim().isNotBlank() }

    // Find first real BEGIN marker (search in original lines for correct position)
    val firstBeginLineNum = rawLines.indexOfFirst { it.trim() == "-----BEGIN CERTIFICATE-----" }
    val hasOverhead = firstBeginLineNum > 0 && rawLines.subList(0, firstBeginLineNum).any { it.trim().isNotEmpty() }

    if (firstBeginLineNum == -1) {
        return CertificateValidationResult.Invalid(
            "No valid PEM BEGIN marker found anywhere in the input.\n" +
                    "Expected to see exactly: -----BEGIN CERTIFICATE----- (5 dashes, correct spelling, case-sensitive)"
        )
    }

    // We'll collect parsing blocks starting from first valid BEGIN
    val blocksStartPositions = mutableListOf<Int>()
    var pos = firstBeginLineNum
    var blockNum = 1
    val blockErrors = mutableListOf<String>()
    val blockWarnings = mutableListOf<String>()

    while (pos < rawLines.size) {
        val beginLine = rawLines[pos].trim()
        if (beginLine != "-----BEGIN CERTIFICATE-----") {
            // Skip lines until next potential BEGIN
            pos++
            continue
        }

        blocksStartPositions.add(pos)

        // Find matching END
        var endPos = pos + 1
        while (endPos < rawLines.size && rawLines[endPos].trim() != "-----END CERTIFICATE-----") {
            endPos++
        }

        if (endPos >= rawLines.size) {
            blockErrors.add(
                "Block #$blockNum (starts line ${pos + 1}):\n" +
                        "  Missing closing marker.\n" +
                        "  Expected on some line after ${pos + 1}: exactly -----END CERTIFICATE-----"
            )
            break
        }

        val endLine = rawLines[endPos].trim()
        if (endLine != "-----END CERTIFICATE-----") {
            blockErrors.add(
                "Block #$blockNum (starts line ${pos + 1}):\n" +
                        "  Invalid END marker on line ${endPos + 1}:\n" +
                        "    Expected: -----END CERTIFICATE----- (5 dashes before/after, correct spelling, case-sensitive, no spaces inside)\n" +
                        "    Found   : '${rawLines[endPos]}'\n" +
                        "    Detected problems:\n" +
                        buildString {
                            if (endLine.count { it == '-' } != 10) append("    • Wrong number of dashes (found ${endLine.count { it == '-' }}, expected 10)\n")
                            if (!endLine.contains("END CERTIFICATE")) append("    • Wrong label or spelling (expected 'CERTIFICATE')\n")
                            if (rawLines[endPos] != endLine) append("    • Extra leading/trailing spaces or characters\n")
                        }
            )
            pos = endPos + 1
            blockNum++
            continue
        }

        // Validate Base64 part
        val base64Start = pos + 1
        val base64End = endPos - 1
        for (i in base64Start..base64End) {
            val line = rawLines[i].trim()
            val lineNum = i + 1
            line.forEachIndexed { col, c ->
                if (c !in "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=") {
                    blockErrors.add(
                        "Block #$blockNum (lines $base64Start–$base64End):\n" +
                                "  Invalid character '$c' on line $lineNum, column ${col + 1}\n" +
                                "  Allowed: only Base64 alphabet (A-Z a-z 0-9 + / =)"
                    )
                }
            }

            if (line.length > 76) {  // Slightly more lenient than 64
                blockWarnings.add(
                    "Block #$blockNum:\n" +
                            "  Line $lineNum exceeds 76 characters (${line.length} chars).\n" +
                            "  While technically allowed, many older parsers prefer ≤64 chars per line."
                )
            }
        }

        pos = endPos + 1
        blockNum++
    }

    if (blockErrors.isNotEmpty()) {
        return CertificateValidationResult.Invalid(
            "Validation failed due to the following issues:\n\n" +
                    blockErrors.joinToString("\n────────────────────\n")
        )
    }

    // If we found at least one block → try full parse
    try {
        val factory = CertificateFactory.getInstance("X.509")
        val certs = factory.generateCertificates(pem.byteInputStream(Charsets.UTF_8))

        if (certs.isEmpty()) {
            return CertificateValidationResult.Invalid(
                "PEM structure looks correct, but no X.509 certificate(s) could be parsed.\n" +
                        "Possible causes:\n" +
                        "• Empty or corrupted Base64 content between markers\n" +
                        "• Invalid DER/ASN.1 encoding inside the certificate data"
            )
        }

        val nonCert = certs.filterNot { it is X509Certificate }
        if (nonCert.isNotEmpty()) {
            return CertificateValidationResult.Invalid(
                "Parsed ${certs.size} item(s), but ${nonCert.size} are not X.509 certificates.\n" +
                        "All items must be valid X.509 certificates."
            )
        }

        // All good → apply warnings
        val allWarnings = buildList {
            if (hasOverhead) {
                add(
                    "Overhead text detected before the first PEM block.\n" +
                            "Location: lines 1–${firstBeginLineNum}.\n" +
                            "This may cause issues in Ingenico Terminals (some terminals reject any content before the BEGIN marker)."
                )
            }
            addAll(blockWarnings)
        }

        return if (allWarnings.isNotEmpty()) {
            CertificateValidationResult.ValidWithWarning(
                allWarnings.joinToString("\n\n")
            )
        } else {
            CertificateValidationResult.Valid
        }

    } catch (e: CertificateException) {
        return CertificateValidationResult.Invalid(
            "PEM markers appear correct, but the certificate data failed to parse as valid X.509:\n" +
                    "  Error: ${e.message ?: "Unknown certificate parsing error"}\n\n" +
                    "Common causes:\n" +
                    "• Corrupted or incomplete Base64 encoding\n" +
                    "• Invalid ASN.1 structure (wrong lengths, missing fields, bad tags)\n" +
                    "• Unsupported certificate version or algorithm OID\n" +
                    "• Malformed public key or extensions"
        )
    } catch (e: Exception) {
        return CertificateValidationResult.Invalid(
            "Unexpected error during PEM processing:\n${e.message ?: e::class.simpleName}"
        )
    }
}