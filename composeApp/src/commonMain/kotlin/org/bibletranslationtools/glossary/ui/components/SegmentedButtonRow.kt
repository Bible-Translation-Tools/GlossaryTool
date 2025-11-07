package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun SegmentedButtonRow(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit
) {
    val backgroundColor = Color(0xFFF2F2F2)
    val shadowColor = Color(0xFFCDCDCD)

    val baseHeight = 48.dp
    val overflowAmount = 1.dp
    val selectedHeight = baseHeight + (overflowAmount * 2)

    Box(
        modifier = Modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(baseHeight)
                .clip(MaterialTheme.shapes.medium)
                .background(backgroundColor)
                .innerShadow(
                    shape = MaterialTheme.shapes.medium,
                    shadow = Shadow(
                        color = shadowColor,
                        offset = DpOffset(0.dp, 0.dp),
                        radius = 4.dp
                    )
                )
                .padding(horizontal = 8.dp)
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex

                CompositionLocalProvider(
                    LocalRippleConfiguration provides RippleConfiguration(
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    SegmentedButton(
                        shape = MaterialTheme.shapes.medium,
                        onClick = { onSelectionChange(index) },
                        selected = selected,
                        colors = SegmentedButtonDefaults.colors(
                            activeContentColor = MaterialTheme.colorScheme.primary,
                            activeContainerColor = MaterialTheme.colorScheme.surface,
                            activeBorderColor = Color.Transparent,
                            inactiveContainerColor = Color.Transparent,
                            inactiveBorderColor = Color.Transparent
                        ),
                        icon = {},
                        interactionSource = remember { MutableInteractionSource() },
                        modifier = Modifier
                            .weight(1f)
                            .height(if (selected) selectedHeight else baseHeight)
                            .zIndex(if (selected) 2f else 0f)
                            .then(if (selected) Modifier
                                .shadow(elevation = 4.dp, shape = MaterialTheme.shapes.medium)
                            else Modifier)
                    ) {
                        Text(
                            text = label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}