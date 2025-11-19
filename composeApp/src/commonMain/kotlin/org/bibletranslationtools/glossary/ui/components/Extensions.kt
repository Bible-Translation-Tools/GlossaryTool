package org.bibletranslationtools.glossary.ui.components

import androidx.compose.runtime.Composable
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.user_role_admin
import glossary.composeapp.generated.resources.user_role_editor
import glossary.composeapp.generated.resources.user_role_owner
import glossary.composeapp.generated.resources.user_role_viewer
import org.bibletranslationtools.glossary.data.api.UserRole
import org.jetbrains.compose.resources.stringResource

@Composable
fun UserRole.localize(): String {
    return when (this) {
        UserRole.OWNER -> stringResource(Res.string.user_role_owner)
        UserRole.ADMIN -> stringResource(Res.string.user_role_admin)
        UserRole.EDITOR -> stringResource(Res.string.user_role_editor)
        UserRole.VIEWER -> stringResource(Res.string.user_role_viewer)
    }
}