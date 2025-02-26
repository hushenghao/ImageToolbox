package ru.tech.imageresizershrinker.domain.image.filters

import ru.tech.imageresizershrinker.domain.model.FilterMask

interface FilterMaskApplier<Image, Path, Color> {

    suspend fun filterByMask(
        filterMask: FilterMask<Image, Path, Color>,
        imageUri: String
    ): Image?

    suspend fun filterByMask(
        filterMask: FilterMask<Image, Path, Color>,
        image: Image
    ): Image?

    suspend fun filterByMasks(
        filterMasks: List<FilterMask<Image, Path, Color>>,
        imageUri: String
    ): Image?

    suspend fun filterByMasks(
        filterMasks: List<FilterMask<Image, Path, Color>>,
        image: Image
    ): Image?

}