package org.bibletranslationtools.glossary.ui.navigation

import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.book_icon
import glossary.composeapp.generated.resources.glossary
import glossary.composeapp.generated.resources.glossary_icon
import glossary.composeapp.generated.resources.read
import glossary.composeapp.generated.resources.resources
import glossary.composeapp.generated.resources.resources_icon
import glossary.composeapp.generated.resources.settings
import glossary.composeapp.generated.resources.settings_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class MainTab(
    val title: StringResource,
    val icon: DrawableResource
) {
    Read(Res.string.read, Res.drawable.book_icon),
    Glossary(Res.string.glossary, Res.drawable.glossary_icon),
    Resources(Res.string.resources, Res.drawable.resources_icon),
    Settings(Res.string.settings, Res.drawable.settings_icon)
}
