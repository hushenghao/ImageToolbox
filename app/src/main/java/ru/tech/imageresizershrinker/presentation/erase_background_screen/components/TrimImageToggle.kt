package ru.tech.imageresizershrinker.presentation.erase_background_screen.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.presentation.root.widget.preferences.PreferenceRowSwitch

@Composable
fun TrimImageToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    PreferenceRowSwitch(
        modifier = modifier,
        title = stringResource(R.string.trim_image),
        subtitle = stringResource(R.string.trim_image_sub),
        checked = checked,
        shape = RoundedCornerShape(24.dp),
        onClick = onCheckedChange,
        startIcon = Icons.Rounded.ContentCut
    )
}