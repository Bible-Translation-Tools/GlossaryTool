package org.bibletranslationtools.glossary.ui.glossary

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import org.bibletranslationtools.glossary.Utils

@Composable
fun GlossaryScreen(component: GlossaryComponent) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(Utils.slideHorizontally())
    ) {
        when (val child = it.instance) {
            is GlossaryComponent.Child.GlossaryIndex -> GlossaryIndexScreen(child.component)
            is GlossaryComponent.Child.GlossaryList -> GlossaryListScreen(child.component)
            is GlossaryComponent.Child.CreateGlossary -> CreateGlossaryScreen(child.component)
            is GlossaryComponent.Child.EditPhrase -> EditPhraseScreen(child.component)
            is GlossaryComponent.Child.ViewPhrase -> ViewPhraseScreen(child.component)
            is GlossaryComponent.Child.ImportGlossary -> ImportGlossaryScreen(child.component)
            is GlossaryComponent.Child.SearchPhrases -> SearchPhrasesScreen(child.component)
            is GlossaryComponent.Child.SelectLanguage -> SelectLanguageScreen(child.component)
        }
    }
}