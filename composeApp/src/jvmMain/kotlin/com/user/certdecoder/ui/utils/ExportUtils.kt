package com.user.certdecoder.ui.utils

import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.prefs.Preferences

private val EXPORT_FILE_NAME_TIMESTAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

private val exportPreferences: Preferences = Preferences.userRoot().node("com/user/certdecoder/export")
private const val LAST_EXPORT_DIRECTORY_KEY = "lastExportDirectory"

/**
 * The folder the user last saved an export to, remembered across app restarts. Falls back to
 * [defaultOutputDirectory] the first time, or if the remembered folder no longer exists.
 */
fun lastUsedExportDirectory(): File {
    val savedPath = exportPreferences.get(LAST_EXPORT_DIRECTORY_KEY, null)
    val savedDirectory = savedPath?.let(::File)

    return if (savedDirectory != null && savedDirectory.isDirectory) {
        savedDirectory
    } else {
        defaultOutputDirectory()
    }
}

/** Remembers the folder a file was just saved to, so the next export dialog defaults there. */
fun rememberExportDirectory(savedFile: File) {
    val directory = if (savedFile.isDirectory) savedFile else savedFile.parentFile
    if (directory != null) {
        exportPreferences.put(LAST_EXPORT_DIRECTORY_KEY, directory.absolutePath)
    }
}

/**
 * The application's install directory: the folder containing the packaged .exe launcher.
 * Falls back to the current working directory when not running from a jpackage app image
 * (e.g. during development via `gradle run`).
 */
fun applicationRootDirectory(): File {
    val launcherCommand = ProcessHandle.current().info().command().orElse(null)
    val launcherFile = launcherCommand?.let(::File)
    val isJavaLauncher = launcherFile?.nameWithoutExtension?.lowercase() in setOf("java", "javaw")

    return if (launcherFile != null && !isJavaLauncher) {
        launcherFile.parentFile ?: File(System.getProperty("user.dir"))
    } else {
        File(System.getProperty("user.dir"))
    }
}

/** The "output" folder in the application's root directory, created on demand if missing. */
fun defaultOutputDirectory(): File =
    File(applicationRootDirectory(), "output").apply { mkdirs() }

fun defaultExportFileName(): String =
    "certificate-report-${EXPORT_FILE_NAME_TIMESTAMP.format(java.time.LocalDateTime.now())}"

fun textReportBytes(text: String): ByteArray = text.toByteArray(Charsets.UTF_8)

/**
 * Renders [text] as a two-column "Field | Value" table, splitting each line on its first colon
 * the same way the on-screen output panel does. Lines with no colon (section headers,
 * separators) span the first column in bold.
 */
fun xlsxReportBytes(text: String): ByteArray {
    XSSFWorkbook().use { workbook ->
        val sheet = workbook.createSheet("Certificate Report")

        val headerFont = workbook.createFont().apply { bold = true }
        val headerStyle = workbook.createCellStyle().apply {
            setFont(headerFont)
            alignment = HorizontalAlignment.LEFT
        }
        val sectionStyle = workbook.createCellStyle().apply { setFont(headerFont) }

        var rowIndex = 0
        sheet.createRow(rowIndex++).apply {
            createCell(0).apply { setCellValue("Field"); cellStyle = headerStyle }
            createCell(1).apply { setCellValue("Value"); cellStyle = headerStyle }
        }

        text.lines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            val separatorIndex = line.indexOf(':')
            val row = sheet.createRow(rowIndex++)
            if (separatorIndex != -1) {
                row.createCell(0).setCellValue(line.substring(0, separatorIndex).trim())
                row.createCell(1).setCellValue(line.substring(separatorIndex + 1).trim())
            } else {
                row.createCell(0).apply {
                    setCellValue(line)
                    cellStyle = sectionStyle
                }
            }
        }

        sheet.setColumnWidth(0, 40 * 256)
        sheet.setColumnWidth(1, 90 * 256)

        return ByteArrayOutputStream().use { out ->
            workbook.write(out)
            out.toByteArray()
        }
    }
}
