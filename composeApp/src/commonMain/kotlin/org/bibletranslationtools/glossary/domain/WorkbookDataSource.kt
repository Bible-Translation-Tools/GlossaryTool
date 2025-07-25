package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor

interface WorkbookDataSource {
    fun read(resource: String): List<Workbook>
}

class WorkbookDataSourceImpl(
    private val resourceContainerAccessor: ResourceContainerAccessor
): WorkbookDataSource {

    override fun read(resource: String): List<Workbook> {
        return resourceContainerAccessor.read(resource)
    }
}