package org.bibletranslationtools.glossary.platform

import kotlinx.io.files.Path
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.domain.DirectoryProvider

expect class ResourceContainerAccessor {
    constructor(directoryProvider: DirectoryProvider)
    fun read(resourceId: String): Resource
    fun read(path: Path): Resource
}