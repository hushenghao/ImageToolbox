package ru.tech.imageresizershrinker.presentation.root.widget.color_picker

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.t8rin.dynamic.theme.ColorTuple
import com.t8rin.dynamic.theme.ColorTupleItem
import com.t8rin.dynamic.theme.PaletteStyle
import com.t8rin.dynamic.theme.rememberColorScheme
import ru.tech.imageresizershrinker.presentation.root.shapes.DavidStarShape
import ru.tech.imageresizershrinker.presentation.root.theme.inverse
import ru.tech.imageresizershrinker.presentation.root.theme.outlineVariant
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.container
import ru.tech.imageresizershrinker.presentation.root.widget.utils.LocalSettingsState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorTuplePreview(
    modifier: Modifier = Modifier,
    isDefaultItem: Boolean = false,
    colorTuple: ColorTuple,
    appColorTuple: ColorTuple,
    onClick: () -> Unit
) {
    val settingsState = LocalSettingsState.current
    ColorTupleItem(
        colorTuple = remember(settingsState.themeStyle, colorTuple) {
            derivedStateOf {
                if (settingsState.themeStyle == PaletteStyle.TonalSpot) {
                    colorTuple
                } else colorTuple.run {
                    copy(secondary = primary, tertiary = primary)
                }
            }
        }.value,
        modifier = modifier
            .aspectRatio(1f)
            .container(
                shape = DavidStarShape,
                color = rememberColorScheme(
                    isDarkTheme = settingsState.isNightMode,
                    amoledMode = settingsState.isDynamicColors,
                    colorTuple = colorTuple,
                    contrastLevel = settingsState.themeContrastLevel,
                    style = settingsState.themeStyle,
                    dynamicColor = settingsState.isDynamicColors,
                    isInvertColors = settingsState.isInvertThemeColors
                ).surfaceVariant.copy(alpha = 0.8f),
                borderColor = MaterialTheme.colorScheme.outlineVariant(0.2f),
                resultPadding = 0.dp
            )
            .combinedClickable(
                onClick = onClick
            )
            .padding(3.dp),
        backgroundColor = Color.Transparent
    ) {
        AnimatedContent(
            targetState = (colorTuple == appColorTuple)
                .and(
                    if (colorTuple in ColorTupleDefaults.defaultColorTuples) {
                        isDefaultItem
                    } else true
                )
        ) { selected ->
            BoxWithConstraints(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(maxWidth * (5 / 9f))
                            .background(
                                color = animateColorAsState(
                                    colorTuple.primary.inverse(
                                        fraction = { cond ->
                                            if (cond) 0.8f
                                            else 0.5f
                                        },
                                        darkMode = colorTuple.primary.luminance() < 0.3f
                                    )
                                ).value,
                                shape = CircleShape
                            )
                    )
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = null,
                        tint = colorTuple.primary,
                        modifier = Modifier.size(maxWidth * (1 / 3f))
                    )
                }
            }
        }
    }
}