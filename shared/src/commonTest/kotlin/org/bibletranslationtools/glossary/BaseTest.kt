package org.bibletranslationtools.glossary

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class BaseTest {

    protected lateinit var testRootDir: Path
    private var originalUserHome: String? = null

    @BeforeTest
    open fun setUp() {
        val systemTempDir = System.getProperty("java.io.tmpdir")
        testRootDir = Path(Path(systemTempDir), this::class.simpleName + "_" + Random.nextInt(100000))
        SystemFileSystem.createDirectories(testRootDir)

        originalUserHome = System.getProperty("user.home")
        System.setProperty("user.home", testRootDir.toString())
    }

    @AfterTest
    open fun tearDown() {
        originalUserHome?.let { System.setProperty("user.home", it) }
        deleteDirRecursively(testRootDir)
    }

    private fun deleteDirRecursively(path: Path) {
        if (!SystemFileSystem.exists(path)) return
        if (SystemFileSystem.metadataOrNull(path)?.isDirectory == true) {
            val children = SystemFileSystem.list(path)
            for (child in children) {
                deleteDirRecursively(child)
            }
        }
        SystemFileSystem.delete(path)
    }
}
