package org.bibletranslationtools.glossary.platform

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.sink
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import java.io.ByteArrayInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

actual fun extractZip(bytes: ByteArray, dir: Path) {
    val byteArrayInputStream = ByteArrayInputStream(bytes)
    ZipInputStream(byteArrayInputStream).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
            val targetPath = Path(dir, entry.name)
            if (entry.isDirectory) {
                SystemFileSystem.createDirectories(targetPath)
            } else {
                SystemFileSystem.sink(targetPath).buffered().use { sink ->
                    sink.write(zis.readBytes())
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }
    }
}

actual fun zipDirectory(source: Path, target: PlatformFile) {
    if (!SystemFileSystem.exists(source)
        || SystemFileSystem.metadataOrNull(source)?.isDirectory != true) {
        throw IllegalArgumentException("Source directory does not exist or is not a directory: $source")
    }

    val sink = target.sink().buffered()
    val zipOutputStream = ZipOutputStream(sink.asOutputStream())

    zipOutputStream.use { zos ->
        SystemFileSystem.list(source).forEach { path ->
            zipRecursively(source, path, zos)
        }
    }
}

private fun zipRecursively(rootDir: Path, currentPath: Path, zos: ZipOutputStream) {
    val metadata = SystemFileSystem.metadataOrNull(currentPath) ?: return

    val entryName = currentPath.toString()
        .removePrefix(rootDir.toString())
        .trimStart(SystemPathSeparator)
        .replace(SystemPathSeparator, '/')

    if (metadata.isDirectory) {
        val dirEntry = ZipEntry("$entryName/")
        zos.putNextEntry(dirEntry)
        zos.closeEntry()

        SystemFileSystem.list(currentPath).forEach { child ->
            zipRecursively(rootDir, child, zos)
        }
    } else if (metadata.isRegularFile) {
        val fileEntry = ZipEntry(entryName)
        zos.putNextEntry(fileEntry)

        SystemFileSystem.source(currentPath).buffered().use { fileSource ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fileSource.readAtMostTo(buffer).also { bytesRead = it } != -1) {
                zos.write(buffer, 0, bytesRead)
            }
        }
        zos.closeEntry()
    }
}
