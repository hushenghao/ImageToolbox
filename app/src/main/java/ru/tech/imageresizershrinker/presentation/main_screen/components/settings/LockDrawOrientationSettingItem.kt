package ru.tech.imageresizershrinker.presentation.main_screen.components.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ScreenLockRotation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.presentation.root.widget.preferences.PreferenceRowSwitch
import ru.tech.imageresizershrinker.presentation.root.widget.utils.LocalSettingsState

@Composable
fun LockDrawOrientationSettingItem(
    onClick: (Boolean) -> Unit,
    shape: Shape = ContainerShapeDefaults.defaultShape,
    modifier: Modifier = Modifier.padding(start = 8.dp, end = 8.dp)
) {
    val settingsState = LocalSettingsState.current
    PreferenceRowSwitch(
        shape = shape,
        modifier = modifier,
        onClick = onClick,
        title = stringResource(R.string.lock_draw_orientation),
        subtitle = stringResource(R.string.lock_draw_orientation_sub),
        checked = settingsState.lockDrawOrientation,
        startIcon = Icons.Rounded.ScreenLockRotation
    )
}