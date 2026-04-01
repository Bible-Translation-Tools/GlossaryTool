package org.bibletranslationtools.glossary

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.user_role_admin
import glossary.composeapp.generated.resources.user_role_editor
import glossary.composeapp.generated.resources.user_role_owner
import glossary.composeapp.generated.resources.user_role_viewer
import org.bibletranslationtools.glossary.data.api.UserRole
import org.jetbrains.compose.resources.stringResource

@Composable
fun Int.pxToDp(): Dp {
    val density = LocalDensity.current
    return with(density) { this@pxToDp.toDp() }
}

@Composable
fun UserRole.localize(): String {
    return when (this) {
        UserRole.VIEWER -> stringResource(Res.string.user_role_viewer)
        UserRole.EDITOR -> stringResource(Res.string.user_role_editor)
        UserRole.OWNER -> stringResource(Res.string.user_role_owner)
        UserRole.ADMIN -> stringResource(Res.string.user_role_admin)
    }
}

fun Modifier.positionAwareImePadding() = composed {
    var consumePadding by remember { mutableIntStateOf(0) }
    this@positionAwareImePadding
        .onGloballyPositioned { coordinates ->
            val rootCoordinate = coordinates.findRootCoordinates()
            val bottom = coordinates.positionInWindow().y + coordinates.size.height
            consumePadding = (rootCoordinate.size.height - bottom).toInt()
        }
        .consumeWindowInsets(PaddingValues(bottom = consumePadding.pxToDp()))
        .imePadding()
}
