package org.bibletranslationtools.glossary

import androidx.compose.runtime.Composable
import glossary.shared.generated.resources.Res
import glossary.shared.generated.resources.user_role_admin
import glossary.shared.generated.resources.user_role_editor
import glossary.shared.generated.resources.user_role_owner
import glossary.shared.generated.resources.user_role_viewer
import org.bibletranslationtools.glossary.data.api.UserRole
import org.jetbrains.compose.resources.stringResource

@Composable
fun UserRole.localize(): String {
    return when (this) {
        UserRole.VIEWER -> stringResource(Res.string.user_role_viewer)
        UserRole.EDITOR -> stringResource(Res.string.user_role_editor)
        UserRole.OWNER -> stringResource(Res.string.user_role_owner)
        UserRole.ADMIN -> stringResource(Res.string.user_role_admin)
    }
}

