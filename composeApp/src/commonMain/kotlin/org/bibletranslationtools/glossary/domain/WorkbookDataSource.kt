package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor

interface WorkbookDataSource {
    fun read(filename: String): Resource?
}

class WorkbookDataSourceImpl(
    private val resourceContainerAccessor: ResourceContainerAccessor
): WorkbookDataSource {

    override fun read(filename: String): Resource? {
        return resourceContainerAccessor.read(filename)
    }
}