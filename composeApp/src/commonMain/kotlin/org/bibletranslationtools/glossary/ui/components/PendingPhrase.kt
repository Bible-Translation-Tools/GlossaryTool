package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.edited
import glossary.composeapp.generated.resources.new
import glossary.composeapp.generated.resources.review
import org.bibletranslationtools.glossary.data.api.PendingPhrase
import org.bibletranslationtools.glossary.data.api.ReviewStatus
import org.jetbrains.compose.resources.stringResource

@Composable
fun PendingPhrase(
    pendingPhrase: PendingPhrase,
    adminsCount: Int,
    onView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val approvedProgress by remember(pendingPhrase.reviews) {
        mutableFloatStateOf(
            pendingPhrase.reviews.count {
                it.status == ReviewStatus.APPROVED
            } / adminsCount.toFloat()
        )
    }

    val rejectedProgress by remember(pendingPhrase.reviews) {
        mutableFloatStateOf(
            pendingPhrase.reviews.count {
                it.status == ReviewStatus.REJECTED
            } / adminsCount.toFloat()
        )
    }

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        SegmentedProgressBar(
            progress1 = approvedProgress,
            progress2 = rejectedProgress,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pendingPhrase.phrase.spelling,
                    fontSize = 25.sp
                )
                Text(
                    text = pendingPhrase.phrase.phrase,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(
                        if (pendingPhrase.original == null) {
                            Res.string.new
                        } else Res.string.edited
                    ),
                    fontSize = 14.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = pendingPhrase.user.emoji,
                    fontSize = 25.sp
                )
                Text(
                    text = pendingPhrase.user.username
                )
                TextButton(
                    onClick = onView
                ) {
                    Text(
                        text = stringResource(Res.string.review)
                    )
                }
            }
        }
    }
}