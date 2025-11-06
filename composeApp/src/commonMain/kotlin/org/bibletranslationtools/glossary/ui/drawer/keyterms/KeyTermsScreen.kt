package org.bibletranslationtools.glossary.ui.drawer.keyterms

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import org.bibletranslationtools.glossary.Utils

@Composable
fun KeyTermsScreen(component: KeyTermsComponent) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(Utils.slideHorizontally())
    ) {
        when (val child = it.instance) {
            is KeyTermsComponent.Child.Index -> KeyTermsIndexScreen(child.component)
            is KeyTermsComponent.Child.ViewPhrase -> ViewPhraseScreen(child.component)
            is KeyTermsComponent.Child.EditPhrase -> EditPhraseScreen(child.component)
            is KeyTermsComponent.Child.CreatePhrase -> CreatePhraseScreen(child.component)
        }
    }
}