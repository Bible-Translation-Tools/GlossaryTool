package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedProgressBar(
    progress1: Float,
    progress2: Float,
    modifier: Modifier = Modifier,
    color1: Color = MaterialTheme.colorScheme.tertiary,
    color2: Color = MaterialTheme.colorScheme.error,
    trackColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val totalProgress = (progress1 + progress2).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(trackColor)
            .height(6.dp)
            .fillMaxWidth()
    ) {
        if (totalProgress > 0f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(totalProgress)
                    .fillMaxHeight()
            ) {
                if (progress1 > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(progress1)
                            .fillMaxHeight()
                            .background(color1)
                    )
                }

                if (progress2 > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(progress2)
                            .fillMaxHeight()
                            .background(color2)
                    )
                }
            }
        }
    }
}