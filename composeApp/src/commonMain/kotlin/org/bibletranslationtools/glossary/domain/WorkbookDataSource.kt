package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor

interface WorkbookDataSource {
    fun read(resourceId: String): Resource
}

class WorkbookDataSourceImpl(
    private val resourceContainerAccessor: ResourceContainerAccessor
): WorkbookDataSource {

    override fun read(resourceId: String): Resource {
        return resourceContainerAccessor.read(resourceId)
    }
}