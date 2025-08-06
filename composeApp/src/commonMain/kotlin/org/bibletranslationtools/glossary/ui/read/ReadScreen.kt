package org.bibletranslationtools.glossary.ui.read

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import org.bibletranslationtools.glossary.Utils

@Composable
fun ReadScreen(component: ReadComponent) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(Utils.slideVertically())
    ) {
        when (val child = it.instance) {
            is ReadComponent.Child.Index -> ReadIndexScreen(child.component)
            is ReadComponent.Child.Browse -> BrowseScreen(child.component)
        }
    }
}