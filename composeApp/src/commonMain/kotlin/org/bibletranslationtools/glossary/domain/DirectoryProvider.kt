package org.bibletranslationtools.glossary.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun saveSource(file: Path, fileName: String = file.name): Path
    suspend fun readFile(fileName: String): String?
    suspend fun readFile(file: Path): String?

    suspend fun deleteFile(file: Path)
    suspend fun deleteFile(fileName: String)

    suspend fun createTempFile(prefix: String, suffix: String): Path
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

    override suspend fun saveSource(file: Path, fileName: String): Path {
        return withContext(Dispatchers.IO) {
            saveFile(file, sources, fileName)
        }
    }

    private fun saveFile(file: Path, dir: Path, fileName: String): Path {
        val targetFile = Path(dir, fileName)
        SystemFileSystem.source(file).buffered().use { inputSource ->
            SystemFileSystem.sink(targetFile).buffered().use { outputSink ->
                inputSource.transferTo(outputSink)
            }
        }
        return targetFile
    }

    override suspend fun readFile(fileName: String): String? {
        val file = Path(sources, fileName)
        return readFile(file)
    }

    override suspend fun readFile(file: Path): String? {
        return try {
            withContext(Dispatchers.IO) {
                SystemFileSystem.source(file).buffered().use { source ->
                    source.readString()
                }
            }
        } catch (e: Exception) {
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

    override suspend fun createTempFile(prefix: String, suffix: String): Path {
        val name = prefix + Utils.randomString(8) + suffix
        val file = Path(tempDir, name)
        SystemFileSystem.sink(file).buffered().use {
            // it.write(ByteArray(0)) // Could write empty bytes or just close
        }
        return file
    }
}