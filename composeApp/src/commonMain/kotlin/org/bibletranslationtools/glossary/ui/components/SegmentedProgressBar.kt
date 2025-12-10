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
    segment1: Int,
    segment2: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val rest = (total - segment1 - segment2).coerceAtLeast(0)

    if (total == 0) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outlineVariant)
                .height(6.dp)
                .fillMaxWidth()
        )
        return
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .height(6.dp)
    ) {
        if (segment1 > 0) {
            Box(
                modifier = Modifier
                    .weight(segment1.toFloat())
                    .fillMaxHeight()
                    .background(Color(0xFF4CAF50))
            )
        }

        if (segment2 > 0) {
            Box(
                modifier = Modifier
                    .weight(segment2.toFloat())
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.error)
            )
        }

        if (rest > 0) {
            Box(
                modifier = Modifier
                    .weight(rest.toFloat())
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}