package ru.tech.imageresizershrinker.presentation.main_screen.components.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.presentation.root.widget.controls.EnhancedSliderItem
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.presentation.root.widget.utils.LocalSettingsState

@Composable
fun EmojisCountSettingItem(
    updateEmojisCount: (Int) -> Unit,
    shape: Shape = ContainerShapeDefaults.centerShape,
    modifier: Modifier = Modifier
        .padding(horizontal = 8.dp)
) {
    val settingsState = LocalSettingsState.current
    EnhancedSliderItem(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme
            .secondaryContainer
            .copy(alpha = 0.2f),
        value = settingsState.emojisCount.coerceAtLeast(1),
        title = stringResource(R.string.emojis_count),
        icon = Icons.Outlined.EmojiEmotions,
        valueRange = 1f..5f,
        steps = 3,
        onValueChange = {},
        internalStateTransformation = {
            it.toInt()
        },
        onValueChangeFinished = {
            updateEmojisCount(it.toInt())
        }
    )
}