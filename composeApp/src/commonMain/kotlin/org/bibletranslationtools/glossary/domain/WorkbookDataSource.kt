package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.platform.readResourceContainer

interface WorkbookDataSource {
    fun read(
        language: String,
        resource: String
    ): List<Workbook>
}

class WorkbookDataSourceImpl: WorkbookDataSource {

    override fun read(language: String, resource: String): List<Workbook> {
        return readResourceContainer(language, resource)
    }
}