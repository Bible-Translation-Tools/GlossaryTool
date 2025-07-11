package org.bibletranslationtools.glossary.platform

import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.domain.DirectoryProvider

expect class ResourceContainerAccessor {
    constructor(directoryProvider: DirectoryProvider)
    fun read(resource: String): List<Workbook>
}