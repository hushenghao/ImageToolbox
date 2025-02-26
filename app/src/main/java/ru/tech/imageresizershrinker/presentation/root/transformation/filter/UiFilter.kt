@file:Suppress("UNCHECKED_CAST")

package ru.tech.imageresizershrinker.presentation.root.transformation.filter

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import ru.tech.imageresizershrinker.domain.image.filters.Filter
import ru.tech.imageresizershrinker.domain.image.filters.FilterParam

sealed class UiFilter<T>(
    @StringRes val title: Int,
    val paramsInfo: List<FilterParam> = listOf(),
    override val value: T
) : Filter<Bitmap, T> {

    constructor(
        @StringRes title: Int,
        valueRange: ClosedFloatingPointRange<Float>,
        value: T,
    ) : this(
        title = title,
        paramsInfo = listOf(
            FilterParam(valueRange = valueRange)
        ),
        value = value
    )

    fun <T : Any> copy(value: T): UiFilter<*> {
        if (this.value == null) return newInstance()
        if (this.value!!::class.simpleName != value::class.simpleName) return newInstance()
        return when (this) {
            is UiBrightnessFilter -> UiBrightnessFilter(value as Float)
            is UiContrastFilter -> UiContrastFilter(value as Float)
            is UiHueFilter -> UiHueFilter(value as Float)
            is UiSaturationFilter -> UiSaturationFilter(value as Float)
            is UiColorFilter -> UiColorFilter(value as Color)
            is UiExposureFilter -> UiExposureFilter(value as Float)
            is UiWhiteBalanceFilter -> UiWhiteBalanceFilter(value as Pair<Float, Float>)
            is UiMonochromeFilter -> UiMonochromeFilter(value as Float)
            is UiGammaFilter -> UiGammaFilter(value as Float)
            is UiHazeFilter -> UiHazeFilter(value as Pair<Float, Float>)
            is UiSepiaFilter -> UiSepiaFilter(value as Float)
            is UiSharpenFilter -> UiSharpenFilter(value as Float)
            is UiNegativeFilter -> UiNegativeFilter()
            is UiSolarizeFilter -> UiSolarizeFilter(value as Float)
            is UiVibranceFilter -> UiVibranceFilter(value as Float)
            is UiBlackAndWhiteFilter -> UiBlackAndWhiteFilter()
            is UiCrosshatchFilter -> UiCrosshatchFilter(value as Pair<Float, Float>)
            is UiSobelEdgeDetectionFilter -> UiSobelEdgeDetectionFilter(value as Float)
            is UiHalftoneFilter -> UiHalftoneFilter(value as Float)
            is UiCGAColorSpaceFilter -> UiCGAColorSpaceFilter()
            is UiGaussianBlurFilter -> UiGaussianBlurFilter(value as Float)
            is UiBilaterialBlurFilter -> UiBilaterialBlurFilter(value as Float)
            is UiBoxBlurFilter -> UiBoxBlurFilter(value as Float)
            is UiEmbossFilter -> UiEmbossFilter(value as Float)
            is UiLaplacianFilter -> UiLaplacianFilter()
            is UiVignetteFilter -> UiVignetteFilter(value as Pair<Float, Float>)
            is UiKuwaharaFilter -> UiKuwaharaFilter(value as Float)
            is UiStackBlurFilter -> UiStackBlurFilter(value as Pair<Float, Int>)
            is UiDilationFilter -> UiDilationFilter(value as Float)
            is UiColorMatrixFilter -> UiColorMatrixFilter(value as FloatArray)
            is UiOpacityFilter -> UiOpacityFilter(value as Float)
            is UiSketchFilter -> UiSketchFilter()
            is UiToonFilter -> UiToonFilter()
            is UiPosterizeFilter -> UiPosterizeFilter(value as Float)
            is UiSmoothToonFilter -> UiSmoothToonFilter(value as Triple<Float, Float, Float>)
            is UiLookupFilter -> UiLookupFilter(value as Float)
            is UiNonMaximumSuppressionFilter -> UiNonMaximumSuppressionFilter()
            is UiWeakPixelFilter -> UiWeakPixelFilter()
            is UiColorBalanceFilter -> UiColorBalanceFilter(value as FloatArray)
            is UiConvolution3x3Filter -> UiConvolution3x3Filter(value as FloatArray)
            is UiFalseColorFilter -> UiFalseColorFilter(value as Pair<Color, Color>)
            is UiFastBlurFilter -> UiFastBlurFilter(value as Pair<Float, Int>)
            is UiLuminanceThresholdFilter -> UiLuminanceThresholdFilter(value as Float)
            is UiRGBFilter -> UiRGBFilter(value as Color)
            is UiZoomBlurFilter -> UiZoomBlurFilter(value as Triple<Float, Float, Float>)
            is UiSwirlDistortionFilter -> UiSwirlDistortionFilter(value as Pair<Float, Float>)
            is UiBulgeDistortionFilter -> UiBulgeDistortionFilter(value as Pair<Float, Float>)
            is UiSphereRefractionFilter -> UiSphereRefractionFilter(value as Pair<Float, Float>)
            is UiGlassSphereRefractionFilter -> UiGlassSphereRefractionFilter(value as Pair<Float, Float>)
            is UiHighlightsAndShadowsFilter -> UiHighlightsAndShadowsFilter(value as Pair<Float, Float>)
            is UiPixelationFilter -> UiPixelationFilter(value as Float)
            is UiEnhancedPixelationFilter -> UiEnhancedPixelationFilter(value as Float)
            is UiStrokePixelationFilter -> UiStrokePixelationFilter(value as Float)
            is UiCirclePixelationFilter -> UiCirclePixelationFilter(value as Float)
            is UiDiamondPixelationFilter -> UiDiamondPixelationFilter(value as Float)
            is UiEnhancedCirclePixelationFilter -> UiEnhancedCirclePixelationFilter(value as Float)
            is UiEnhancedDiamondPixelationFilter -> UiEnhancedDiamondPixelationFilter(value as Float)
            is UiReplaceColorFilter -> UiReplaceColorFilter(value as Triple<Float, Color, Color>)
            is UiRemoveColorFilter -> UiRemoveColorFilter(value as Pair<Float, Color>)
        }
    }

    fun newInstance(): UiFilter<*> {
        return when (this) {
            is UiBrightnessFilter -> UiBrightnessFilter()
            is UiContrastFilter -> UiContrastFilter()
            is UiHueFilter -> UiHueFilter()
            is UiSaturationFilter -> UiSaturationFilter()
            is UiColorFilter -> UiColorFilter()
            is UiExposureFilter -> UiExposureFilter()
            is UiWhiteBalanceFilter -> UiWhiteBalanceFilter()
            is UiMonochromeFilter -> UiMonochromeFilter()
            is UiGammaFilter -> UiGammaFilter()
            is UiHazeFilter -> UiHazeFilter()
            is UiSepiaFilter -> UiSepiaFilter()
            is UiSharpenFilter -> UiSharpenFilter()
            is UiNegativeFilter -> UiNegativeFilter()
            is UiSolarizeFilter -> UiSolarizeFilter()
            is UiVibranceFilter -> UiVibranceFilter()
            is UiBlackAndWhiteFilter -> UiBlackAndWhiteFilter()
            is UiCrosshatchFilter -> UiCrosshatchFilter()
            is UiSobelEdgeDetectionFilter -> UiSobelEdgeDetectionFilter()
            is UiHalftoneFilter -> UiHalftoneFilter()
            is UiCGAColorSpaceFilter -> UiCGAColorSpaceFilter()
            is UiGaussianBlurFilter -> UiGaussianBlurFilter()
            is UiBilaterialBlurFilter -> UiBilaterialBlurFilter()
            is UiBoxBlurFilter -> UiBoxBlurFilter()
            is UiEmbossFilter -> UiEmbossFilter()
            is UiLaplacianFilter -> UiLaplacianFilter()
            is UiVignetteFilter -> UiVignetteFilter()
            is UiKuwaharaFilter -> UiKuwaharaFilter()
            is UiStackBlurFilter -> UiStackBlurFilter()
            is UiDilationFilter -> UiDilationFilter()
            is UiColorMatrixFilter -> UiColorMatrixFilter()
            is UiOpacityFilter -> UiOpacityFilter()
            is UiSketchFilter -> UiSketchFilter()
            is UiToonFilter -> UiToonFilter()
            is UiPosterizeFilter -> UiPosterizeFilter()
            is UiSmoothToonFilter -> UiSmoothToonFilter()
            is UiLookupFilter -> UiLookupFilter()
            is UiNonMaximumSuppressionFilter -> UiNonMaximumSuppressionFilter()
            is UiWeakPixelFilter -> UiWeakPixelFilter()
            is UiColorBalanceFilter -> UiColorBalanceFilter()
            is UiConvolution3x3Filter -> UiConvolution3x3Filter()
            is UiFalseColorFilter -> UiFalseColorFilter()
            is UiFastBlurFilter -> UiFastBlurFilter()
            is UiLuminanceThresholdFilter -> UiLuminanceThresholdFilter()
            is UiRGBFilter -> UiRGBFilter()
            is UiZoomBlurFilter -> UiZoomBlurFilter()
            is UiSwirlDistortionFilter -> UiSwirlDistortionFilter()
            is UiBulgeDistortionFilter -> UiBulgeDistortionFilter()
            is UiSphereRefractionFilter -> UiSphereRefractionFilter()
            is UiGlassSphereRefractionFilter -> UiGlassSphereRefractionFilter()
            is UiHighlightsAndShadowsFilter -> UiHighlightsAndShadowsFilter()
            is UiPixelationFilter -> UiPixelationFilter()
            is UiEnhancedPixelationFilter -> UiEnhancedPixelationFilter()
            is UiStrokePixelationFilter -> UiStrokePixelationFilter()
            is UiCirclePixelationFilter -> UiCirclePixelationFilter()
            is UiDiamondPixelationFilter -> UiDiamondPixelationFilter()
            is UiEnhancedCirclePixelationFilter -> UiEnhancedCirclePixelationFilter()
            is UiEnhancedDiamondPixelationFilter -> UiEnhancedDiamondPixelationFilter()
            is UiReplaceColorFilter -> UiReplaceColorFilter()
            is UiRemoveColorFilter -> UiRemoveColorFilter()
        }
    }

    companion object {
        val groupedEntries by lazy {
            listOf(
                listOf(
                    UiHueFilter(),
                    UiColorFilter(),
                    UiSaturationFilter(),
                    UiVibranceFilter(),
                    UiRGBFilter(),
                    UiReplaceColorFilter(),
                    UiRemoveColorFilter(),
                    UiFalseColorFilter(),
                    UiCGAColorSpaceFilter(),
                    UiMonochromeFilter(),
                    UiSepiaFilter(),
                    UiNegativeFilter(),
                    UiBlackAndWhiteFilter(),
                    UiColorMatrixFilter(),
                    UiColorBalanceFilter()
                ),
                listOf(
                    UiBrightnessFilter(),
                    UiContrastFilter(),
                    UiExposureFilter(),
                    UiWhiteBalanceFilter(),
                    UiGammaFilter(),
                    UiHighlightsAndShadowsFilter(),
                    UiSolarizeFilter(),
                    UiHazeFilter()
                ),
                listOf(
                    UiSharpenFilter(),
                    UiCrosshatchFilter(),
                    UiSobelEdgeDetectionFilter(),
                    UiHalftoneFilter(),
                    UiEmbossFilter(),
                    UiLaplacianFilter(),
                    UiVignetteFilter(),
                    UiKuwaharaFilter(),
                    UiDilationFilter(),
                    UiOpacityFilter(),
                    UiToonFilter(),
                    UiSmoothToonFilter(),
                    UiSketchFilter(),
                    UiPosterizeFilter(),
                    UiLookupFilter(),
                    UiNonMaximumSuppressionFilter(),
                    UiWeakPixelFilter(),
                    UiConvolution3x3Filter(),
                    UiLuminanceThresholdFilter()
                ),
                listOf(
                    UiGaussianBlurFilter(),
                    UiBoxBlurFilter(),
                    UiBilaterialBlurFilter(),
                    UiFastBlurFilter(),
                    UiStackBlurFilter(),
                    UiZoomBlurFilter(),
                ),
                listOf(
                    UiPixelationFilter(),
                    UiEnhancedPixelationFilter(),
                    UiDiamondPixelationFilter(),
                    UiEnhancedDiamondPixelationFilter(),
                    UiCirclePixelationFilter(),
                    UiEnhancedCirclePixelationFilter(),
                    UiStrokePixelationFilter()
                ),
                listOf(
                    UiSwirlDistortionFilter(),
                    UiBulgeDistortionFilter(),
                    UiSphereRefractionFilter(),
                    UiGlassSphereRefractionFilter()
                )
            )
        }
    }

}

fun Filter<Bitmap, *>.toUiFilter(): UiFilter<*> = when (this) {
    is Filter.BilaterialBlur -> UiBilaterialBlurFilter(value)
    is Filter.BlackAndWhite -> UiBlackAndWhiteFilter()
    is Filter.BoxBlur -> UiBoxBlurFilter(value)
    is Filter.Brightness -> UiBrightnessFilter(value)
    is Filter.BulgeDistortion -> UiBulgeDistortionFilter(value)
    is Filter.CGAColorSpace -> UiCGAColorSpaceFilter()
    is Filter.CirclePixelation -> UiCirclePixelationFilter(value)
    is Filter.ColorBalance -> UiColorBalanceFilter(value)
    is Filter.Color -> UiColorFilter(value as Color)
    is Filter.ColorMatrix -> UiColorMatrixFilter(value)
    is Filter.Contrast -> UiContrastFilter(value)
    is Filter.Convolution3x3 -> UiConvolution3x3Filter(value)
    is Filter.Crosshatch -> UiCrosshatchFilter(value)
    is Filter.DiamondPixelation -> UiDiamondPixelationFilter(value)
    is Filter.Dilation -> UiDilationFilter(value)
    is Filter.Emboss -> UiEmbossFilter(value)
    is Filter.EnhancedCirclePixelation -> UiEnhancedCirclePixelationFilter(value)
    is Filter.EnhancedDiamondPixelation -> UiEnhancedDiamondPixelationFilter(value)
    is Filter.EnhancedPixelation -> UiEnhancedPixelationFilter(value)
    is Filter.Exposure -> UiExposureFilter(value)
    is Filter.FalseColor<*, *> -> UiFalseColorFilter(value as Pair<Color, Color>)
    is Filter.FastBlur -> UiFastBlurFilter(value)
    is Filter.Gamma -> UiGammaFilter(value)
    is Filter.GaussianBlur -> UiGaussianBlurFilter(value)
    is Filter.GlassSphereRefraction -> UiGlassSphereRefractionFilter(value)
    is Filter.Halftone -> UiHalftoneFilter(value)
    is Filter.Haze -> UiHazeFilter(value)
    is Filter.HighlightsAndShadows -> UiHighlightsAndShadowsFilter(value)
    is Filter.Hue -> UiHueFilter(value)
    is Filter.Kuwahara -> UiKuwaharaFilter(value)
    is Filter.Laplacian -> UiLaplacianFilter()
    is Filter.Lookup -> UiLookupFilter(value)
    is Filter.LuminanceThreshold -> UiLuminanceThresholdFilter(value)
    is Filter.Monochrome -> UiMonochromeFilter(value)
    is Filter.Negative -> UiNegativeFilter()
    is Filter.NonMaximumSuppression -> UiNonMaximumSuppressionFilter()
    is Filter.Opacity -> UiOpacityFilter(value)
    is Filter.Pixelation -> UiPixelationFilter(value)
    is Filter.Posterize -> UiPosterizeFilter(value)
    is Filter.RemoveColor<*, *> -> UiRemoveColorFilter(value as Pair<Float, Color>)
    is Filter.ReplaceColor<*, *> -> UiReplaceColorFilter(value as Triple<Float, Color, Color>)
    is Filter.RGB -> UiRGBFilter(value as Color)
    is Filter.Saturation -> UiSaturationFilter(value)
    is Filter.Sepia -> UiSepiaFilter(value)
    is Filter.Sharpen -> UiSharpenFilter(value)
    is Filter.Sketch -> UiSketchFilter(value)
    is Filter.SmoothToon -> UiSmoothToonFilter(value)
    is Filter.SobelEdgeDetection -> UiSobelEdgeDetectionFilter(value)
    is Filter.Solarize -> UiSolarizeFilter(value)
    is Filter.SphereRefraction -> UiSphereRefractionFilter(value)
    is Filter.StackBlur -> UiStackBlurFilter(value)
    is Filter.StrokePixelation -> UiStrokePixelationFilter(value)
    is Filter.SwirlDistortion -> UiSwirlDistortionFilter(value)
    is Filter.Toon -> UiToonFilter(value)
    is Filter.Vibrance -> UiVibranceFilter(value)
    is Filter.Vignette -> UiVignetteFilter(value)
    is Filter.WeakPixel -> UiWeakPixelFilter()
    is Filter.WhiteBalance -> UiWhiteBalanceFilter(value)
    is Filter.ZoomBlur -> UiZoomBlurFilter(value)

    else -> throw IllegalArgumentException("No UiFilter implementation for interface ${this::class.simpleName}")
}

infix fun Int.paramTo(valueRange: ClosedFloatingPointRange<Float>) = FilterParam(
    title = this,
    valueRange = valueRange
)