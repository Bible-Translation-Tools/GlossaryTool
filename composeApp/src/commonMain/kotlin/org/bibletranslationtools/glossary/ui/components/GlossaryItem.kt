package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import glossary.composeapp.generated.resources.users_count
import glossary.composeapp.generated.resources.words_count
import org.bibletranslationtools.glossary.ui.glossary.GlossaryItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GlossaryItem(
    item: GlossaryItem,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else Color.Unspecified

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, borderColor),
        shape = MaterialTheme.shapes.large,
        onClick = onSelected,
        modifier = modifier
    ) {
        Box {
            RadioButton(
                selected = isSelected,
                onClick = onSelected,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.glossary.code,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlossaryInfo(
                        title = stringResource(Res.string.words_count, item.phraseCount),
                        icon = painterResource(Res.drawable.match_word)
                    )

                    GlossaryInfo(
                        title = stringResource(Res.string.users_count, item.userCount),
                        icon = painterResource(Res.drawable.group)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (isSelected) {
                        Text(
                            text = stringResource(Res.string.active),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.W500,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}