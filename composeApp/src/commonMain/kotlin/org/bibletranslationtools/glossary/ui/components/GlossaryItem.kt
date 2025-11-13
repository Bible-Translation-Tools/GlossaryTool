package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.active
import glossary.composeapp.generated.resources.group
import glossary.composeapp.generated.resources.match_word
import glossary.composeapp.generated.resources.select
import glossary.composeapp.generated.resources.share
import glossary.composeapp.generated.resources.users_count
import glossary.composeapp.generated.resources.words_count
import org.bibletranslationtools.glossary.ui.drawer.settings.GlossaryItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GlossaryItem(
    item: GlossaryItem,
    isSelected: Boolean,
    isActive: Boolean,
    onSelected: () -> Unit,
    onSelectedSave: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.outlineVariant
    val borderSize = if (isSelected) 2.dp else 1.dp

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(borderSize, borderColor),
        shape = MaterialTheme.shapes.large,
        onClick = onSelected,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            Text(
                text = item.glossary.code,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isActive) {
                    Text(
                        text = stringResource(Res.string.active),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else Color.Unspecified,
                        fontWeight = FontWeight.W500,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (isSelected || !isActive) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onSelected
                    )
                } else {
                    IconButton(
                        onClick = onSelected
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "checked"
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp)
                .offset(y = -(12).dp)
        ) {
            Text(
                text = item.glossary.sourceLanguage.name,
                fontWeight = FontWeight.W500,
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = "to"
            )
            Text(
                text = item.glossary.targetLanguage.name,
                fontWeight = FontWeight.W500,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            GlossaryInfo(
                title = stringResource(Res.string.words_count, item.phraseCount),
                icon = painterResource(Res.drawable.match_word)
            )

            GlossaryInfo(
                title = stringResource(Res.string.users_count, item.userCount),
                icon = painterResource(Res.drawable.group)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSelected) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp)
            ) {
                ElevatedButton(
                    onClick = onShare,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                        .height(36.dp)
                ) {
                    Text(stringResource(Res.string.share))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onSelectedSave,
                    enabled = !isActive,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                        .height(36.dp)
                ) {
                    Text(stringResource(Res.string.select))
                }
            }
        }
    }
}