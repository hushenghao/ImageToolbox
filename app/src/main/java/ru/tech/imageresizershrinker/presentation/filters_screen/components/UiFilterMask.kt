package ru.tech.imageresizershrinker.presentation.filters_screen.components

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import ru.tech.imageresizershrinker.domain.image.draw.PathPaint
import ru.tech.imageresizershrinker.domain.image.filters.Filter
import ru.tech.imageresizershrinker.domain.model.FilterMask

data class UiFilterMask(
    override val filters: List<Filter<Bitmap, *>>,
    override val maskPaints: List<PathPaint<Path, Color>>,
    override val isInverseFillType: Boolean
) : FilterMask<Bitmap, Path, Color>