package org.bibletranslationtools.glossary

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import org.bibletranslationtools.glossary.platform.appDirPath
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Application logger that writes to both console and file.
 * 
 * Log levels:
 * - DEBUG: Detailed information for debugging
 * - WARNING: Warning messages (recoverable issues)
 * - ERROR: Error messages (crashes, fatal issues)
 */
object AppLogger {
    private const val LOG_DIR = "logs"
    private const val LOG_FILE = "app.log"
    private const val MAX_LOG_SIZE = 5 * 1024 * 1024 // 5 MB

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    
    private val rootDir: Path
        get() = Path(appDirPath)
    
    private val logDir: Path
        get() = Path(rootDir, LOG_DIR)
    
    private val logFile: Path
        get() = Path(logDir, LOG_FILE)

    private fun ensureLogDir() {
        if (!SystemFileSystem.exists(logDir)) {
            SystemFileSystem.createDirectories(logDir)
        }
    }

    private fun ensureLogFile() {
        ensureLogDir()
        if (!SystemFileSystem.exists(logFile)) {
            SystemFileSystem.sink(logFile).buffered().use { }
        }
    }

    private fun writeToFile(message: String) {
        ensureLogFile()
        
        try {
            // Read existing content and append new message
            val existingContent = try {
                SystemFileSystem.source(logFile).buffered().use { source ->
                    source.readString()
                }
            } catch (e: Exception) {
                ""
            }
            
            val newContent = if (existingContent.isNotEmpty()) {
                "$existingContent\n$message"
            } else {
                message
            }
            
            // Check if we need to rotate (simple check based on character count)
            if (newContent.length > MAX_LOG_SIZE) {
                rotateLog()
                SystemFileSystem.sink(logFile).buffered().use { sink ->
                    sink.writeString(message)
                }
            } else {
                SystemFileSystem.sink(logFile).buffered().use { sink ->
                    sink.writeString(if (existingContent.isNotEmpty()) "\n$message" else message)
                }
            }
        } catch (e: Exception) {
            System.err.println("Failed to write to log file: ${e.message}")
        }
    }

    private fun rotateLog() {
        try {
            val rotatedFile = Path(logDir, "app.log.old")
            if (SystemFileSystem.exists(rotatedFile)) {
                SystemFileSystem.delete(rotatedFile, mustExist = false)
            }
            // Copy content to old file then delete current
            val content = SystemFileSystem.source(logFile).buffered().use { source ->
                source.readString()
            }
            SystemFileSystem.sink(rotatedFile).buffered().use { sink ->
                sink.writeString(content)
            }
            SystemFileSystem.delete(logFile, mustExist = false)
        } catch (e: Exception) {
            System.err.println("Failed to rotate log file: ${e.message}")
        }
    }

    /**
     * Log debug message
     */
    fun debug(tag: String, message: String) {
        val formatted = formatMessage("DEBUG", tag, message)
        println(formatted)
        writeToFile(formatted)
    }

    /**
     * Log warning message
     */
    fun warning(tag: String, message: String) {
        val formatted = formatMessage("WARNING", tag, message)
        println(formatted)
        writeToFile(formatted)
    }

    /**
     * Log error message (crashes, exceptions)
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) {
            "$message\n${throwable.stackTraceToString()}"
        } else {
            message
        }
        val formatted = formatMessage("ERROR", tag, fullMessage)
        System.err.println(formatted)
        writeToFile(formatted)
    }

    private fun formatMessage(level: String, tag: String, message: String): String {
        return "[${dateFormat.format(Date())}] [$level] [$tag] $message"
    }

    /**
     * Read last N lines from the log file
     */
    suspend fun readLogLines(lines: Int = 100): String = withContext(Dispatchers.IO) {
        try {
            if (!SystemFileSystem.exists(logFile)) {
                return@withContext "No log file found"
            }
            val content = SystemFileSystem.source(logFile).buffered().use { source ->
                source.readString()
            }
            val allLines = content.lines()
            val startIndex = maxOf(0, allLines.size - lines)
            allLines.subList(startIndex, allLines.size).joinToString("\n")
        } catch (e: Exception) {
            "Error reading log: ${e.message}"
        }
    }

    /**
     * Clear the log file
     */
    suspend fun clearLog() = withContext(Dispatchers.IO) {
        try {
            if (SystemFileSystem.exists(logFile)) {
                SystemFileSystem.delete(logFile, mustExist = false)
            }
        } catch (e: Exception) {
            System.err.println("Error clearing log: ${e.message}")
        }
    }

    /**
     * Get the log file path
     */
    fun getLogFilePath(): String = logFile.toString()

    /**
     * Setup global uncaught exception handler.
     * Call this at app startup to catch all uncaught exceptions.
     */
    fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            error(
                tag = "UNCAUGHT_EXCEPTION",
                message = "Uncaught exception on thread: ${thread.name}",
                throwable = throwable
            )
            // Call the original handler if it exists
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}

/**
 * Extension function for debug logging
 */
fun Any.logD(message: String) {
    val tag = this::class.simpleName ?: "Unknown"
    AppLogger.debug(tag, message)
}

/**
 * Extension function for warning logging
 */
fun Any.logW(message: String) {
    val tag = this::class.simpleName ?: "Unknown"
    AppLogger.warning(tag, message)
}

/**
 * Extension function for error logging
 */
fun Any.logE(message: String, throwable: Throwable? = null) {
    val tag = this::class.simpleName ?: "Unknown"
    AppLogger.error(tag, message, throwable)
}
