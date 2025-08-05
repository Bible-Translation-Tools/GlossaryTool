package org.bibletranslationtools.glossary.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.bibletranslationtools.glossary.Utils
import org.bibletranslationtools.glossary.platform.appDirPath

interface DirectoryProvider {
    val rootDir: Path
    val sources: Path
    val tempDir: Path

    suspend fun saveSource(bytes: ByteArray, fileName: String): Path
    suspend fun saveSource(file: Path, fileName: String = file.name): Path
    suspend fun readSource(fileName: String): String?
    suspend fun readFile(file: Path): String?

    suspend fun deleteFile(file: Path)
    suspend fun deleteFile(fileName: String)
    suspend fun copyFileToDir(sourceFile: Path, destDir: Path)

    suspend fun createTempFile(prefix: String, suffix: String, parent: Path? = null): Path
    suspend fun createTempDir(prefix: String, parent: Path? = null): Path
    suspend fun clearTempDir()
}

class DirectoryProviderImpl : DirectoryProvider {

    override val rootDir = Path(appDirPath)
    override val sources: Path
        get() {
            val dir = Path(rootDir, "sources")
            if (!SystemFileSystem.exists(dir)) {
                SystemFileSystem.createDirectories(dir) // Creates parent directories if needed
            }
            return dir
        }

    override val tempDir: Path
        get() {
            val dir = Path(rootDir, "temp")
            if (!SystemFileSystem.exists(dir)) {
                SystemFileSystem.createDirectories(dir)
            }
            return dir
        }

    override suspend fun readSource(fileName: String): String? {
        val file = Path(sources, fileName)
        return readFile(file)
    }

    override suspend fun saveSource(bytes: ByteArray, fileName: String): Path {
        return writeFile(bytes, sources, fileName)
    }

    override suspend fun saveSource(file: Path, fileName: String): Path {
        return withContext(Dispatchers.IO) {
            writeFile(file, sources, fileName)
        }
    }

    private fun writeFile(bytes: ByteArray, dir: Path, fileName: String): Path {
        val file = Path(dir, fileName)
        SystemFileSystem.sink(file).buffered().use { sink ->
            sink.write(bytes)
        }
        return file
    }

    private fun writeFile(file: Path, dir: Path, fileName: String): Path {
        val targetFile = Path(dir, fileName)
        SystemFileSystem.source(file).buffered().use { inputSource ->
            SystemFileSystem.sink(targetFile).buffered().use { outputSink ->
                inputSource.transferTo(outputSink)
            }
        }
        return targetFile
    }

    override suspend fun readFile(file: Path): String? {
        return try {
            withContext(Dispatchers.IO) {
                SystemFileSystem.source(file).buffered().use { source ->
                    source.readString()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun deleteFile(file: Path) {
        SystemFileSystem.delete(file, mustExist = false)
    }

    override suspend fun deleteFile(fileName: String) {
        val file = Path(sources,fileName)
        deleteFile(file)
    }

    override suspend fun copyFileToDir(sourceFile: Path, destDir: Path) {
        if (!SystemFileSystem.exists(destDir)
            || SystemFileSystem.metadataOrNull(destDir)?.isDirectory != true) {
            throw IllegalArgumentException("Destination directory does not exist or is not a directory: $destDir")
        }

        val destFile = Path(destDir, sourceFile.name)

        try {
            SystemFileSystem.source(sourceFile).buffered().use { source ->
                SystemFileSystem.sink(destFile).buffered().use { sink ->
                    source.transferTo(sink)
                }
            }
        } catch (e: IOException) {
            println("Error copying file: ${e.message}")
        }
    }

    override suspend fun createTempFile(
        prefix: String,
        suffix: String,
        parent: Path?
    ): Path {
        val name = prefix + Utils.randomString(8) + suffix
        val parentDir = if (parent != null) {
            Path(tempDir, parent.name)
        } else tempDir
        val file = Path(parentDir, name)
        SystemFileSystem.sink(file).buffered().use {
            // it.write(ByteArray(0)) // Could write empty bytes or just close
        }
        return file
    }

    override suspend fun createTempDir(prefix: String, parent: Path?): Path {
        val name = prefix + Utils.randomString(8)
        val parentDir = if (parent != null) {
            Path(tempDir, parent.name)
        } else tempDir
        val dir = Path(parentDir, name)
        SystemFileSystem.createDirectories(dir)
        return dir
    }

    override suspend fun clearTempDir() {
        try {
            deleteDirRecursively(tempDir)
        } catch (e: Exception) {
            println("Error clearing temp directory: ${e.message}")
        }
    }

    private fun deleteDirRecursively(path: Path) {
        if (!SystemFileSystem.exists(path)) return

        if (SystemFileSystem.metadataOrNull(path)?.isDirectory != true) {
            SystemFileSystem.delete(path)
            return
        }

        val children = SystemFileSystem.list(path)

        if (children.isEmpty()) {
            SystemFileSystem.delete(path)
        } else {
            children.forEach { dir ->
                deleteDirRecursively(dir)
            }
        }
    }
}