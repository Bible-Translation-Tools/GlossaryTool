package org.bibletranslationtools.glossary.ui

import com.arkivanov.decompose.ComponentContext
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Workbook

interface ParentContext {
    fun onBackClick()
    fun dismissDrawer()
    fun openSettings()
    fun openKeyTerms(book: Workbook, chapter: Chapter)
}

abstract class AppComponent(
    componentContext: ComponentContext,
    private val parentContext: ParentContext
) : ParentContext, ComponentContext by componentContext {

    override fun onBackClick() {
        parentContext.onBackClick()
    }

    override fun dismissDrawer() {
        parentContext.dismissDrawer()
    }

    override fun openSettings() {
        parentContext.openSettings()
    }

    override fun openKeyTerms(book: Workbook, chapter: Chapter) {
        parentContext.openKeyTerms(book, chapter)
    }
}