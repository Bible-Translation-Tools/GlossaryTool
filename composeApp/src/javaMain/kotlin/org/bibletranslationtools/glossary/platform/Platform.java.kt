package org.bibletranslationtools.glossary.platform

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

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
