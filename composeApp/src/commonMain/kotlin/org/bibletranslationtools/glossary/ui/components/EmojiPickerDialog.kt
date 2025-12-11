package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.cancel
import glossary.composeapp.generated.resources.emoji_activity
import glossary.composeapp.generated.resources.emoji_food
import glossary.composeapp.generated.resources.emoji_gestures
import glossary.composeapp.generated.resources.emoji_nature
import glossary.composeapp.generated.resources.emoji_objects
import glossary.composeapp.generated.resources.emoji_smileys
import glossary.composeapp.generated.resources.emoji_travel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class EmojiCategory(
    val title: StringResource,
    val icon: String,
    val emojis: List<String>
)

private val allEmojiCategories = listOf(
    EmojiCategory(
        title = Res.string.emoji_smileys,
        icon = "😀",
        emojis = listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
            "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😭",
            "😙", "🥲", "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭",
            "🤫", "🤔", "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄",
            "😬", "🤥", "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕",
            "🤢", "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳",
            "😎", "🤓", "🧐", "😕", "😟", "🙁", "😮", "😯", "😲", "😱",
            "😳", "🥺", "😦", "😧", "😨", "😰", "😥", "😢"
        )
    ),
    EmojiCategory(
        title = Res.string.emoji_gestures,
        icon = "👍",
        emojis = listOf(
            "👋", "🤚", "🖐", "✋", "🖖", "👌", "🤏", "✌️", "🤞", "🤟",
            "🤘", "🤙", "👈", "👉", "👆", "👇", "👍", "👎", "✊", "💪",
            "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "👀",
            "👄", "💋"
        )
    ),
    EmojiCategory(
        title = Res.string.emoji_nature,
        icon = "🐻",
        emojis = listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
            "🦁", "🐮", "🐷", "🐽", "🐸", "🐵", "🙈", "🙉", "🙊", "🐒",
            "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉", "🦇",
            "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌", "🐞", "🐜",
            "🦟", "🦗", "🕷", "🕸", "🦂", "🐢", "🐍", "🦎", "🦖", "🦕",
            "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳",
            "🌲", "🌳", "🌴", "🌵", "🌷", "🌸", "🌹", "🌺", "🌻", "🌼"
        )
    ),
    EmojiCategory(
        title = Res.string.emoji_food,
        icon = "🍔",
        emojis = listOf(
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🍈",
            "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑", "🥦",
            "🥬", "🥒", "🌶", "🌽", "🥕", "🥔", "🍠", "🥐", "🥯", "🍞",
            "🥖", "🥨", "🧀", "🥚", "🍳", "🥞", "🥓", "🥩", "🍗", "🍖",
            "🌭", "🍔", "🍟", "🍕", "🥪", "🥙", "🌮", "🌯", "🥗", "🥘",
            "🥫", "🍝", "🍜", "🍲", "🍛", "🍣", "🍱", "🥟", "🍤", "🍙",
            "🍚", "🍘", "🍥", "🥠", "🍦", "🍧", "🍨", "🍩", "🍪", "🎂",
            "🍺", "🍻", "🥂", "🍷", "🥃", "🍸", "🍹", "🍾", "☕", "🍵"
        )
    ),
    EmojiCategory(
        title = Res.string.emoji_activity,
        icon = "⚽",
        emojis = listOf(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
            "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🥅", "⛳", "🪁",
            "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹", "🛼", "🛷", "⛸",
            "🥌", "🎿", "⛷", "🏂", "🪂", "🏋️", "🤼", "🤸", "⛹️", "🤺",
            "🎮", "🕹", "🎰", "🎲", "🧩", "🧸", "♠️", "♥️", "♦️", "♣️",
            "♟", "🃏", "🀄", "🎨", "🧵", "🧶", "🎹", "🎷", "🎺", "🎸"
        )
    ),
    EmojiCategory(
        title = Res.string.emoji_travel,
        icon = "🚗",
        emojis = listOf(
            "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
            "🛻", "🚚", "🚛", "🚜", "🏍️", "🛵", "🚲", "🦼", "🦽", "🦺",
            "🚨", "🚔", "🚍", "🚘", "🚖", "🚡", "🚠", "🚟", "🚃", "🚋",
            "🚞", "🚝", "🚄", "🚅", "🚈", "🚂", "🚆", "🚇", "🚊", "🚉",
            "✈️", "🛫", "🛬", "🛩️", "💺", "🛰️", "🚀", "🛸", "🚁", "🛶",
            "⛵", "🚤", "🛥️", "🛳️", "⛴️", "🚢", "⚓", "🚧", "⛽", "🚏"
        )
    ),
    EmojiCategory(
        title = Res.string.emoji_objects,
        icon = "💡",
        emojis = listOf(
            "⌚", "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "🖲️", "🕹️",
            "🗜️", "💽", "💾", "💿", "📀", "📼", "📷", "📸", "📹", "🎥",
            "📽️", "🎞️", "📞", "☎️", "📟", "📠", "📺", "📻", "🎙️", "🎚️",
            "🎛️", "🧭", "⏱️", "⏲️", "⏰", "🕰️", "⌛", "⏳", "📡", "🔋",
            "🔌", "💡", "🔦", "🕯️", "🪔", "🧯", "🛢️", "💸", "💵", "💴",
            "💶", "💷", "💰", "💳", "💎", "⚖️", "🧰", "🔧", "🔨", "⚒️",
            "🛠️", "⛏️", "🔩", "⚙️", "🧱", "⛓️", "🧲", "🔫", "💣", "🧨"
        )
    )
)

@Composable
fun EmojiPickerDialog(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val listState = rememberLazyGridState()

    LaunchedEffect(selectedTabIndex) {
        listState.scrollToItem(0)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column {
                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    allEmojiCategories.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(stringResource(category.title)) },
                            icon = { Text(category.icon) }
                        )
                    }
                }

                val currentEmojis = allEmojiCategories[selectedTabIndex].emojis

                Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 45.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        state = listState
                    ) {
                        items(currentEmojis) { emoji ->
                            EmojiGridItem(
                                emoji = emoji,
                                onClick = {
                                    onEmojiSelected(emoji)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(8.dp)
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        }
    }
}

@Composable
fun EmojiGridItem(emoji: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
    ) {
        Text(text = emoji, fontSize = 28.sp)
    }
}