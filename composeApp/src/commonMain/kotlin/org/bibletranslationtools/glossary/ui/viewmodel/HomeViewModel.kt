package org.bibletranslationtools.glossary.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import glossary.composeapp.generated.resources.Res
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.bibletranslationtools.glossary.data.GlossaryDataSource

class HomeViewModel(
    private val glossaryDataSource: GlossaryDataSource
) : ScreenModel {

    fun insert() {
        screenModelScope.launch {
            val date = Clock.System.now()
            glossaryDataSource.insert("test", "test", date.epochSeconds)
        }
    }

    fun getAll() {
        screenModelScope.launch {
            glossaryDataSource.getAll().collectLatest { records ->
                records.forEach {
                    println(it.code)
                }
            }
        }
    }

    fun readResource() {
        screenModelScope.launch {
            val bytes = Res.readBytes("files/catalog.json")
            println(bytes.size)
            println(bytes)
        }
    }
}