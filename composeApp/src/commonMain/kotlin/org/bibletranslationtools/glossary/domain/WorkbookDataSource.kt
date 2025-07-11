package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor

interface WorkbookDataSource {
    fun read(
        language: String,
        resource: String
    ): List<Workbook>
}

class WorkbookDataSourceImpl(
    private val resourceContainerAccessor: ResourceContainerAccessor
): WorkbookDataSource {

    override fun read(language: String, resource: String): List<Workbook> {
        return resourceContainerAccessor.read(language, resource)
    }
}