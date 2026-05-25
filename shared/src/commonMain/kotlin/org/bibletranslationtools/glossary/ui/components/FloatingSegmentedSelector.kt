package org.bibletranslationtools.glossary.ui.components

import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * A segmented selector component with a "floating pill" visual effect and sliding animation.
 *
 * @param options A list of types representing the selectable options.
 * @param selectedOption The currently selected option.
 * @param onOptionSelected Lambda to call when a new option is selected.
 */
@Composable
fun <T> FloatingSegmentedSelector(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    height: Dp = 72.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable (option: T, isSelected: Boolean) -> Unit
) {
    val pillHeight = height - 12.dp
    val trackHeight = pillHeight - 8.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val containerWidth = maxWidth
        val itemWidth = remember(options.size, containerWidth) {
            containerWidth / options.size
        }

        val selectedIndex = remember(options, selectedOption) {
            options.indexOf(selectedOption).coerceAtLeast(0)
        }

        var displayedIndex by remember { mutableIntStateOf(selectedIndex) }
        val targetOffset = (itemWidth * selectedIndex)

        val animatedOffset by animateDpAsState(
            targetValue = targetOffset,
            animationSpec = tween(
                durationMillis = 100,
                easing = EaseOutQuad
            ),
            finishedListener = {
                displayedIndex = selectedIndex
            },
            label = "pill_slide_animation"
        )

        if (selectedIndex != displayedIndex && animatedOffset.value == targetOffset.value) {
            displayedIndex = selectedIndex
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .align(Alignment.Center)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = shape
                )
                .clip(shape)
                .innerShadow(
                    shape = MaterialTheme.shapes.medium,
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.surfaceDim,
                        offset = DpOffset(0.dp, 0.dp),
                        radius = 4.dp
                    )
                )
        )

        Surface(
            modifier = Modifier
                .width(itemWidth)
                .height(pillHeight)
                .align(Alignment.CenterStart)
                .offset { IntOffset(x = animatedOffset.roundToPx(), y = 0) }
                .padding(4.dp)
                .shadow(elevation = 8.dp, shape = shape)
                .clip(shape),
            color = MaterialTheme.colorScheme.surface,
            shape = shape,
        ) {
            val option = options.getOrElse(displayedIndex) { options.first() }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                content(option, true)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillHeight)
                .align(Alignment.Center)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onOptionSelected(option) },
                            contentAlignment = Alignment.Center
                        ) {
                            content(option, false)
                        }
                    } else {
                        Spacer(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}