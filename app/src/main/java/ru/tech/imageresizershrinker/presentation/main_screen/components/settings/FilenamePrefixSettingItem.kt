package ru.tech.imageresizershrinker.presentation.main_screen.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.presentation.root.icons.material.CreateAlt
import ru.tech.imageresizershrinker.presentation.root.icons.material.FileSettings
import ru.tech.imageresizershrinker.presentation.root.icons.material.Prefix
import ru.tech.imageresizershrinker.presentation.root.theme.outlineVariant
import ru.tech.imageresizershrinker.presentation.root.widget.controls.EnhancedButton
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.alertDialogBorder
import ru.tech.imageresizershrinker.presentation.root.widget.preferences.PreferenceItem
import ru.tech.imageresizershrinker.presentation.root.widget.utils.LocalSettingsState

@Composable
fun FilenamePrefixSettingItem(
    updateFilenamePrefix: (String) -> Unit,
    shape: Shape = ContainerShapeDefaults.topShape,
    modifier: Modifier = Modifier.padding(start = 8.dp, end = 8.dp)
) {
    val context = LocalContext.current
    val settingsState = LocalSettingsState.current
    var showChangeFilenameDialog by rememberSaveable { mutableStateOf(false) }

    PreferenceItem(
        shape = shape,
        onClick = {
            showChangeFilenameDialog = true
        },
        enabled = !settingsState.randomizeFilename,
        title = stringResource(R.string.prefix),
        subtitle = (settingsState.filenamePrefix.takeIf { it.isNotEmpty() }
            ?: stringResource(R.string.default_prefix)),
        color = MaterialTheme.colorScheme
            .secondaryContainer
            .copy(alpha = 0.2f),
        endIcon = Icons.Rounded.CreateAlt,
        icon = Icons.Filled.Prefix,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp)
    )
    if (showChangeFilenameDialog) {
        var value by remember {
            mutableStateOf(
                settingsState
                    .filenamePrefix
                    .takeIf {
                        it.isNotEmpty()
                    } ?: context.getString(R.string.default_prefix)
            )
        }
        AlertDialog(
            modifier = Modifier
                .width(340.dp)
                .padding(16.dp)
                .alertDialogBorder(),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showChangeFilenameDialog = false },
            icon = {
                Icon(Icons.Rounded.FileSettings, null)
            },
            title = {
                Text(stringResource(R.string.prefix))
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        placeholder = {
                            Text(
                                text = stringResource(R.string.default_prefix),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        value = value,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center
                        ),
                        onValueChange = {
                            value = it
                        }
                    )
                }
            },
            confirmButton = {
                EnhancedButton(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                        alpha = if (settingsState.isNightMode) 0.5f
                        else 1f
                    ),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = {
                        updateFilenamePrefix(value.trim())
                        showChangeFilenameDialog = false
                    },
                    borderColor = MaterialTheme.colorScheme.outlineVariant(
                        onTopOf = MaterialTheme.colorScheme.secondaryContainer
                    ),
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}