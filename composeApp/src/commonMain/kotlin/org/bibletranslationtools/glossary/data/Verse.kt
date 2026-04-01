package org.bibletranslationtools.glossary.data

import kotlinx.serialization.Serializable

@Serializable
data class Verse(val number: String, val text: String)
