package ru.tech.imageresizershrinker.data.image

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.core.graphics.BitmapCompat
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.data.image.filters.FadeSide
import ru.tech.imageresizershrinker.data.image.filters.SideFadeFilter
import ru.tech.imageresizershrinker.data.image.filters.StackBlurFilter
import ru.tech.imageresizershrinker.domain.image.ImageManager
import ru.tech.imageresizershrinker.domain.image.Transformation
import ru.tech.imageresizershrinker.domain.image.filters.Filter
import ru.tech.imageresizershrinker.domain.image.filters.provider.FilterProvider
import ru.tech.imageresizershrinker.domain.model.CombiningParams
import ru.tech.imageresizershrinker.domain.model.ImageData
import ru.tech.imageresizershrinker.domain.model.ImageFormat
import ru.tech.imageresizershrinker.domain.model.ImageInfo
import ru.tech.imageresizershrinker.domain.model.ImageWithSize
import ru.tech.imageresizershrinker.domain.model.IntegerSize
import ru.tech.imageresizershrinker.domain.model.Preset
import ru.tech.imageresizershrinker.domain.model.ResizeType
import ru.tech.imageresizershrinker.domain.model.withSize
import ru.tech.imageresizershrinker.domain.saving.FileController
import ru.tech.imageresizershrinker.domain.saving.model.ImageSaveTarget
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt
import coil.transform.Transformation as CoilTransformation


class AndroidImageManager @Inject constructor(
    private val context: Context,
    private val fileController: FileController,
    private val imageLoader: ImageLoader,
    private val filterProvider: FilterProvider<Bitmap>
) : ImageManager<Bitmap, ExifInterface> {

    override fun getFilterProvider(): FilterProvider<Bitmap> = filterProvider

    private fun toCoil(transformation: Transformation<Bitmap>): CoilTransformation {
        return object : CoilTransformation {
            override val cacheKey: String
                get() = transformation.cacheKey

            override suspend fun transform(
                input: Bitmap,
                size: Size
            ): Bitmap = transformation.transform(input, size)
        }
    }

    override suspend fun transform(
        image: Bitmap,
        transformations: List<Transformation<Bitmap>>,
        originalSize: Boolean
    ): Bitmap? = withContext(Dispatchers.IO) {
        val request = ImageRequest
            .Builder(context)
            .data(image)
            .transformations(
                transformations.map(::toCoil)
            )
            .apply {
                if (originalSize) size(Size.ORIGINAL)
            }
            .build()

        return@withContext imageLoader.execute(request).drawable?.toBitmap()
    }

    override suspend fun filter(
        image: Bitmap,
        filters: List<Filter<Bitmap, *>>,
        originalSize: Boolean
    ): Bitmap? = transform(
        image = image,
        transformations = filters.map { filterProvider.filterToTransformation(it) },
        originalSize = originalSize
    )

    override suspend fun transform(
        image: Bitmap,
        transformations: List<Transformation<Bitmap>>,
        size: IntegerSize
    ): Bitmap? = withContext(Dispatchers.IO) {
        val request = ImageRequest
            .Builder(context)
            .data(image)
            .transformations(
                transformations.map(::toCoil)
            )
            .size(size.width, size.height)
            .build()

        return@withContext imageLoader.execute(request).drawable?.toBitmap()
    }

    override suspend fun filter(
        image: Bitmap,
        filters: List<Filter<Bitmap, *>>,
        size: IntegerSize
    ): Bitmap? = transform(
        image = image,
        transformations = filters.map { filterProvider.filterToTransformation(it) },
        size = size
    )

    override suspend fun getImage(
        uri: String,
        originalSize: Boolean
    ): ImageData<Bitmap, ExifInterface>? = withContext(Dispatchers.IO) {
        return@withContext kotlin.runCatching {
            imageLoader.execute(
                ImageRequest
                    .Builder(context)
                    .data(uri)
                    .apply {
                        if (originalSize) size(Size.ORIGINAL)
                    }
                    .build()
            ).drawable?.toBitmap()
        }.getOrNull()?.let { bitmap ->
            val fd = context.contentResolver.openFileDescriptor(uri.toUri(), "r")
            val exif = fd?.fileDescriptor?.let { ExifInterface(it) }
            fd?.close()
            ImageData(
                image = bitmap,
                imageInfo = ImageInfo(
                    width = bitmap.width,
                    height = bitmap.height,
                    imageFormat = ImageFormat[getExtension(uri)]
                ),
                metadata = exif
            )
        }
    }

    override suspend fun getImage(data: Any, originalSize: Boolean): Bitmap? {
        return imageLoader.execute(
            ImageRequest
                .Builder(context)
                .data(data)
                .apply {
                    if (originalSize) size(Size.ORIGINAL)
                }
                .build()
        ).drawable?.toBitmap()
    }

    override suspend fun createFilteredPreview(
        image: Bitmap,
        imageInfo: ImageInfo,
        filters: List<Filter<Bitmap, *>>,
        onGetByteCount: (Int) -> Unit
    ): Bitmap = createPreview(
        image = image,
        imageInfo = imageInfo,
        transformations = filters.map { filterProvider.filterToTransformation(it) },
        onGetByteCount = onGetByteCount
    )

    override fun getImageAsync(
        uri: String,
        originalSize: Boolean,
        onGetImage: (ImageData<Bitmap, ExifInterface>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val bmp = kotlin.runCatching {
            imageLoader.enqueue(
                ImageRequest
                    .Builder(context)
                    .data(uri)
                    .apply {
                        if (originalSize) size(Size.ORIGINAL)
                    }
                    .target { drawable ->
                        drawable.toBitmap()?.let { it ->
                            val fd = context.contentResolver.openFileDescriptor(uri.toUri(), "r")
                            val exif = fd?.fileDescriptor?.let { ExifInterface(it) }
                            fd?.close()
                            ImageData(
                                image = it,
                                imageInfo = ImageInfo(
                                    width = it.width,
                                    height = it.height,
                                    imageFormat = ImageFormat[getExtension(uri)]
                                ),
                                metadata = exif
                            )
                        }?.let(onGetImage)
                    }.build()
            )
        }
        bmp.exceptionOrNull()?.let(onError)
    }

    override suspend fun resize(
        image: Bitmap,
        width: Int,
        height: Int,
        resizeType: ResizeType
    ): Bitmap? = withContext(Dispatchers.IO) {

        val widthInternal = width.takeIf { it > 0 } ?: image.width
        val heightInternal = height.takeIf { it > 0 } ?: image.height

        return@withContext when (resizeType) {
            ResizeType.Explicit -> {
                image.createScaledBitmap(
                    width = widthInternal,
                    height = heightInternal
                )
            }

            ResizeType.Flexible -> {
                flexibleResize(
                    image = image,
                    max = max(widthInternal, heightInternal)
                )
            }

            is ResizeType.Limits -> {
                resizeType.resizeWithLimits(
                    image = image,
                    width = widthInternal,
                    height = heightInternal
                )
            }

            is ResizeType.CenterCrop -> {
                resizeType.resizeWithCenterCrop(
                    image = image,
                    w = widthInternal,
                    h = heightInternal
                )
            }
        }
    }

    private fun flexibleResize(image: Bitmap, max: Int): Bitmap {
        return kotlin.runCatching {
            if (image.height >= image.width) {
                val aspectRatio = image.width.toDouble() / image.height.toDouble()
                val targetWidth = (max * aspectRatio).toInt()
                image.createScaledBitmap(targetWidth, max)
            } else {
                val aspectRatio = image.height.toDouble() / image.width.toDouble()
                val targetHeight = (max * aspectRatio).toInt()
                image.createScaledBitmap(max, targetHeight)
            }
        }.getOrNull() ?: image
    }

    private suspend fun ResizeType.Limits.resizeWithLimits(
        image: Bitmap,
        width: Int,
        height: Int
    ): Bitmap? {
        val limitWidth: Int
        val limitHeight: Int

        if (autoRotateLimitBox && image.aspectRatio < 1f) {
            limitWidth = height
            limitHeight = width
        } else {
            limitWidth = width
            limitHeight = height
        }
        val limitAspectRatio = limitWidth / limitHeight.toFloat()

        if (image.height > limitHeight || image.width > limitWidth) {
            if (image.aspectRatio > limitAspectRatio) {
                return resize(
                    image = image,
                    width = limitWidth,
                    height = limitWidth,
                    resizeType = ResizeType.Flexible
                )
            } else if (image.aspectRatio < limitAspectRatio) {
                return resize(
                    image = image,
                    width = limitHeight,
                    height = limitHeight,
                    resizeType = ResizeType.Flexible
                )
            } else {
                return resize(
                    image = image,
                    width = limitWidth,
                    height = limitHeight,
                    resizeType = ResizeType.Flexible
                )
            }
        } else {
            return when (this) {
                is ResizeType.Limits.Recode -> image

                is ResizeType.Limits.Zoom -> resize(
                    image = image,
                    width = limitWidth,
                    height = limitHeight,
                    resizeType = ResizeType.Flexible
                )

                is ResizeType.Limits.Skip -> null
            }
        }
    }

    private val Bitmap.aspectRatio: Float get() = width / height.toFloat()


    override fun applyPresetBy(image: Bitmap?, preset: Preset, currentInfo: ImageInfo): ImageInfo {
        if (image == null) return currentInfo


        val rotated = abs(currentInfo.rotationDegrees) % 180 != 0f
        fun Bitmap.width() = if (rotated) height else width
        fun Bitmap.height() = if (rotated) width else height
        fun Int.calc(cnt: Int): Int = (this * (cnt / 100f)).toInt()

        return when (preset) {
            is Preset.Telegram -> {
                currentInfo.copy(
                    width = 512,
                    height = 512,
                    imageFormat = ImageFormat.Png,
                    resizeType = ResizeType.Flexible,
                    quality = 100f
                )
            }

            is Preset.Numeric -> currentInfo.copy(
                quality = preset.value.toFloat(),
                width = image.width().calc(preset.value),
                height = image.height().calc(preset.value),
            )

            is Preset.None -> currentInfo
        }
    }

    override suspend fun getSampledImage(
        uri: String,
        reqWidth: Int,
        reqHeight: Int
    ): ImageData<Bitmap, ExifInterface>? = withContext(Dispatchers.IO) {
        return@withContext imageLoader.execute(
            ImageRequest
                .Builder(context)
                .size(reqWidth, reqHeight)
                .data(uri)
                .build()
        ).drawable?.toBitmap()?.let { bitmap ->
            val fd = context.contentResolver.openFileDescriptor(uri.toUri(), "r")
            val exif = fd?.fileDescriptor?.let { ExifInterface(it) }
            fd?.close()
            ImageData(
                image = bitmap,
                imageInfo = ImageInfo(
                    width = bitmap.width,
                    height = bitmap.height,
                    imageFormat = ImageFormat[getExtension(uri)]
                ),
                metadata = exif
            )
        }
    }

    override suspend fun getImageWithFiltersApplied(
        uri: String,
        filters: List<Filter<Bitmap, *>>,
        originalSize: Boolean
    ): ImageData<Bitmap, ExifInterface>? = getImageWithTransformations(
        uri = uri,
        transformations = filters.map { filterProvider.filterToTransformation(it) },
        originalSize = originalSize
    )

    override suspend fun shareFile(
        byteArray: ByteArray,
        filename: String,
        onComplete: () -> Unit
    ) = withContext(Dispatchers.IO) {
        val imagesFolder = File(context.cacheDir, "images")
        val uri = kotlin.runCatching {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, filename)
            FileOutputStream(file).use {
                it.write(byteArray)
            }
            FileProvider.getUriForFile(
                context,
                context.getString(R.string.file_provider),
                file
            )
        }.getOrNull()
        uri?.let {
            shareUri(
                uri = it.toString(),
                type = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(
                        getExtension(uri.toString())
                    ) ?: "*/*"
            )
        }
        onComplete()
    }

    override suspend fun combineImages(
        imageUris: List<String>,
        combiningParams: CombiningParams,
        imageScale: Float
    ): ImageData<Bitmap, ExifInterface> = withContext(Dispatchers.IO) {
        combiningParams.run {
            val (size, images) = calculateCombinedImageDimensionsAndBitmaps(
                imageUris = imageUris,
                combiningParams = combiningParams
            )

            val bitmaps = images.map { image ->
                if (scaleSmallImagesToLarge && image.shouldUpscale(isHorizontal, size)) {
                    image.upscale(isHorizontal, size)
                } else image
            }

            val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap).apply {
                drawColor(Color.Transparent.toArgb(), PorterDuff.Mode.CLEAR)
                drawColor(backgroundColor)
            }


            var pos = 0
            for (i in imageUris.indices) {
                var bmp = bitmaps[i]

                combiningParams.spacing.takeIf { it < 0 && combiningParams.fadingEdgesMode != null }
                    ?.let {
                        val space = spacing.absoluteValue
                        val bottomFilter =
                            SideFadeFilter((if (isHorizontal) FadeSide.End else FadeSide.Bottom) to space)
                        val topFilter =
                            SideFadeFilter((if (isHorizontal) FadeSide.Start else FadeSide.Top) to space)
                        val filters = if (combiningParams.fadingEdgesMode == 0) {
                            when (i) {
                                0 -> listOf()
                                else -> listOf(topFilter)
                            }
                        } else {
                            when (i) {
                                0 -> listOf(bottomFilter)
                                imageUris.lastIndex -> listOf(topFilter)
                                else -> listOf(topFilter, bottomFilter)
                            }
                        }
                        transform(bmp, filters)?.let { bmp = it }
                    }

                if (isHorizontal) {
                    canvas.drawBitmap(
                        bmp,
                        pos.toFloat(),
                        0f,
                        null
                    )
                } else {
                    canvas.drawBitmap(
                        bmp,
                        0f,
                        pos.toFloat(),
                        null
                    )
                }
                pos += if (isHorizontal) {
                    (bmp.width + spacing).coerceAtLeast(1)
                } else (bmp.height + spacing).coerceAtLeast(1)
            }

            ImageData(
                image = resize(
                    image = bitmap,
                    width = (size.width * imageScale).toInt(),
                    height = (size.height * imageScale).toInt(),
                    resizeType = ResizeType.Flexible
                )!!,
                imageInfo = ImageInfo(
                    width = (size.width * imageScale).toInt(),
                    height = (size.height * imageScale).toInt(),
                )
            )
        }
    }

    override suspend fun calculateCombinedImageDimensions(
        imageUris: List<String>,
        combiningParams: CombiningParams
    ): IntegerSize = calculateCombinedImageDimensionsAndBitmaps(
        imageUris = imageUris,
        combiningParams = combiningParams
    ).first

    private suspend fun calculateCombinedImageDimensionsAndBitmaps(
        imageUris: List<String>,
        combiningParams: CombiningParams
    ): Pair<IntegerSize, List<Bitmap>> = withContext(Dispatchers.IO) {
        combiningParams.run {
            var w = 0
            var h = 0
            var maxHeight = 0
            var maxWidth = 0
            val drawables = imageUris.mapNotNull { uri ->
                imageLoader.execute(
                    ImageRequest
                        .Builder(context)
                        .data(uri)
                        .size(Size.ORIGINAL)
                        .build()
                ).drawable?.toBitmap()?.apply {
                    maxWidth = max(maxWidth, width)
                    maxHeight = max(maxHeight, height)
                }
            }

            drawables.forEachIndexed { index, image ->
                val width = image.width
                val height = image.height

                val spacing = if (index != drawables.lastIndex) spacing else 0

                if (scaleSmallImagesToLarge && image.shouldUpscale(
                        isHorizontal = isHorizontal,
                        size = IntegerSize(maxWidth, maxHeight)
                    )
                ) {
                    val targetHeight: Int
                    val targetWidth: Int

                    if (isHorizontal) {
                        targetHeight = maxHeight
                        targetWidth = (targetHeight * image.aspectRatio).toInt()
                    } else {
                        targetWidth = maxWidth
                        targetHeight = (targetWidth / image.aspectRatio).toInt()
                    }
                    if (isHorizontal) {
                        w += (targetWidth + spacing).coerceAtLeast(1)
                    } else {
                        h += (targetHeight + spacing).coerceAtLeast(1)
                    }
                } else {
                    if (isHorizontal) {
                        w += (width + spacing).coerceAtLeast(1)
                    } else {
                        h += (height + spacing).coerceAtLeast(1)
                    }
                }
            }

            if (isHorizontal) {
                h = maxHeight
            } else {
                w = maxWidth
            }

            IntegerSize(w, h) to drawables
        }
    }

    override suspend fun createCombinedImagesPreview(
        imageUris: List<String>,
        combiningParams: CombiningParams,
        imageFormat: ImageFormat,
        quality: Float,
        onGetByteCount: (Int) -> Unit
    ): ImageWithSize<Bitmap> = withContext(Dispatchers.IO) {
        val imageSize = calculateCombinedImageDimensions(
            imageUris = imageUris,
            combiningParams = combiningParams
        )

        combineImages(
            imageUris = imageUris,
            combiningParams = combiningParams,
            imageScale = 1f
        ).let { (image, imageInfo, _) ->
            return@let createPreview(
                image = image,
                imageInfo = imageInfo.copy(
                    imageFormat = imageFormat,
                    quality = quality
                ),
                transformations = emptyList(),
                onGetByteCount = onGetByteCount
            ) withSize imageSize
        }
    }

    override suspend fun trimEmptyParts(image: Bitmap): Bitmap = BackgroundRemover.trim(image)

    override fun removeBackgroundFromImage(
        image: Bitmap,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Throwable) -> Unit,
        trimEmptyParts: Boolean
    ) {
        kotlin.runCatching {
            BackgroundRemover.bitmapForProcessing(
                bitmap = image,
                scope = CoroutineScope(Dispatchers.IO)
            ) { result ->
                if (result.isSuccess) {
                    result.getOrNull()?.let {
                        if (trimEmptyParts) trimEmptyParts(it)
                        else it
                    }?.let(onSuccess)
                } else result.exceptionOrNull()?.let(onFailure)
            }
        }.exceptionOrNull()?.let(onFailure)
    }

    override suspend fun shareImages(
        uris: List<String>,
        imageLoader: suspend (String) -> ImageData<Bitmap, ExifInterface>?,
        onProgressChange: (Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        var cnt = 0
        val uriList: MutableList<Uri> = mutableListOf()
        uris.forEach { uri ->
            imageLoader(uri)?.let { (image, imageInfo) ->
                cacheImage(
                    image = image,
                    imageInfo = imageInfo
                )?.let { uri ->
                    cnt += 1
                    uriList.add(uri.toUri())
                }
            }
            onProgressChange(cnt)
        }
        onProgressChange(-1)
        shareImageUris(uriList)
    }

    override suspend fun cacheImage(
        image: Bitmap,
        imageInfo: ImageInfo,
        name: String
    ): String? = withContext(Dispatchers.IO) {
        val imagesFolder = File(context.cacheDir, "images")
        return@withContext kotlin.runCatching {
            imagesFolder.mkdirs()
            val saveTarget = ImageSaveTarget<ExifInterface>(
                imageInfo = imageInfo,
                originalUri = "share",
                sequenceNumber = null,
                data = byteArrayOf()
            )

            val file = File(imagesFolder, fileController.constructImageFilename(saveTarget))
            FileOutputStream(file).use {
                it.write(compress(ImageData(image, imageInfo)))
            }
            FileProvider.getUriForFile(context, context.getString(R.string.file_provider), file)
                .also {
                    context.grantUriPermission(
                        context.packageName,
                        it,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
        }.getOrNull()?.toString()
    }

    override suspend fun shareImage(
        imageData: ImageData<Bitmap, ExifInterface>,
        onComplete: () -> Unit,
        name: String
    ) = withContext(Dispatchers.IO) {
        cacheImage(
            image = imageData.image,
            imageInfo = imageData.imageInfo
        )?.let {
            shareUri(
                uri = it,
                type = imageData.imageInfo.imageFormat.type
            )
        }
        onComplete()
    }

    override suspend fun getImageWithTransformations(
        uri: String,
        transformations: List<Transformation<Bitmap>>,
        originalSize: Boolean
    ): ImageData<Bitmap, ExifInterface>? = withContext(Dispatchers.IO) {
        val request = ImageRequest
            .Builder(context)
            .data(uri)
            .transformations(
                transformations.map(::toCoil)
            )
            .apply {
                if (originalSize) size(Size.ORIGINAL)
            }
            .build()
        return@withContext imageLoader.execute(request).drawable?.toBitmap()?.let { bitmap ->
            val fd = context.contentResolver.openFileDescriptor(uri.toUri(), "r")
            val exif = fd?.fileDescriptor?.let { ExifInterface(it) }
            fd?.close()
            ImageData(
                image = bitmap,
                imageInfo = ImageInfo(
                    width = bitmap.width,
                    height = bitmap.height,
                    imageFormat = ImageFormat[getExtension(uri)]
                ),
                metadata = exif
            )
        }
    }

    override suspend fun scaleByMaxBytes(
        image: Bitmap,
        imageFormat: ImageFormat,
        maxBytes: Long
    ): ImageData<Bitmap, ExifInterface>? = withContext(Dispatchers.IO) {
        val maxBytes1 =
            maxBytes - maxBytes
                .times(0.04f)
                .roundToInt()
                .coerceIn(
                    minimumValue = 256,
                    maximumValue = 512
                )

        return@withContext kotlin.runCatching {
            if (
                calculateImageSize(
                    imageData = ImageData(
                        image = image,
                        imageInfo = ImageInfo(imageFormat = imageFormat)
                    )
                ) > maxBytes1
            ) {
                var streamLength = maxBytes1
                var compressQuality = 100
                val bmpStream = ByteArrayOutputStream()
                var newSize = image.width to image.height

                while (streamLength >= maxBytes1) {
                    compressQuality -= 1

                    if (compressQuality < 20) break

                    bmpStream.use {
                        it.flush()
                        it.reset()
                    }
                    bmpStream.write(
                        compress(
                            ImageData(
                                image = image,
                                imageInfo = ImageInfo(
                                    quality = compressQuality.toFloat(),
                                    imageFormat = imageFormat
                                )
                            )
                        )
                    )
                    streamLength = (bmpStream.toByteArray().size).toLong()
                }

                if (compressQuality < 20) {
                    compressQuality = 20
                    while (streamLength >= maxBytes1) {
                        bmpStream.use {
                            it.flush()
                            it.reset()
                        }
                        val temp = resize(
                            image = image,
                            width = (newSize.first * 0.98).toInt(),
                            height = (newSize.second * 0.98).toInt(),
                            resizeType = ResizeType.Explicit
                        )
                        bmpStream.write(
                            temp?.let {
                                newSize = it.width to it.height
                                compress(
                                    ImageData(
                                        image = image,
                                        imageInfo = ImageInfo(
                                            quality = compressQuality.toFloat(),
                                            imageFormat = imageFormat,
                                            width = newSize.first,
                                            height = newSize.second
                                        )
                                    )
                                )
                            }
                        )
                        streamLength = (bmpStream.toByteArray().size).toLong()
                    }
                }
                BitmapFactory.decodeStream(ByteArrayInputStream(bmpStream.toByteArray())) to compressQuality
            } else null
        }.getOrNull()?.let {
            ImageData(
                it.first,
                imageInfo = ImageInfo(
                    width = it.first.width,
                    height = it.first.height,
                    quality = it.second.toFloat()
                )
            )
        }
    }

    override fun canShow(image: Bitmap): Boolean {
        return canShow(image.size())
    }

    private fun canShow(size: Int): Boolean {
        return size < 3096 * 3096 * 3
    }

    override suspend fun scaleUntilCanShow(
        image: Bitmap?
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (image == null) return@withContext null

        var (height, width) = image.run { height to width }

        var iterations = 0
        while (!canShow(size = height * width * 4)) {
            height = (height * 0.85f).roundToInt()
            width = (width * 0.85f).roundToInt()
            iterations++
        }

        return@withContext if (iterations == 0) image
        else resize(
            image = image,
            height = height,
            width = width,
            resizeType = ResizeType.Flexible
        )
    }

    override suspend fun calculateImageSize(imageData: ImageData<Bitmap, ExifInterface>): Long {
        return compress(imageData).size.toLong()
    }

    override suspend fun compress(
        imageData: ImageData<Bitmap, ExifInterface>,
        onImageReadyToCompressInterceptor: suspend (Bitmap) -> Bitmap,
        applyImageTransformations: Boolean
    ): ByteArray = withContext(Dispatchers.IO) {
        val currentImage: Bitmap
        if (applyImageTransformations) {
            currentImage = resize(
                image = rotate(
                    image = imageData.image.apply { setHasAlpha(true) },
                    degrees = imageData.imageInfo.rotationDegrees
                ),
                width = imageData.imageInfo.width,
                height = imageData.imageInfo.height,
                resizeType = imageData.imageInfo.resizeType
            )?.let {
                flip(
                    image = it,
                    isFlipped = imageData.imageInfo.isFlipped
                )
            }?.let {
                onImageReadyToCompressInterceptor(it)
            } ?: return@withContext ByteArray(0)
        } else currentImage = onImageReadyToCompressInterceptor(imageData.image)

        return@withContext ImageCompressor.compress(
            image = currentImage,
            imageFormat = imageData.imageInfo.imageFormat,
            quality = imageData.imageInfo.quality
        )
    }

    override fun flip(image: Bitmap, isFlipped: Boolean): Bitmap {
        return if (isFlipped) {
            val matrix = Matrix().apply { postScale(-1f, 1f, image.width / 2f, image.height / 2f) }
            return Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        } else image
    }

    override fun rotate(image: Bitmap, degrees: Float): Bitmap {
        //TODO: Add free rotation( maybe :) )
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
    }

    private suspend fun compressCenterCrop(
        scaleFactor: Float,
        onImageReadyToCompressInterceptor: suspend (Bitmap) -> Bitmap,
        imageData: ImageData<Bitmap, ExifInterface>
    ): ByteArray = withContext(Dispatchers.IO) {

        val currentImage: Bitmap =
            (imageData.imageInfo.resizeType as? ResizeType.CenterCrop)?.resizeWithCenterCrop(
                image = rotate(
                    image = imageData.image.apply { setHasAlpha(true) },
                    degrees = imageData.imageInfo.rotationDegrees
                ),
                w = imageData.imageInfo.width,
                h = imageData.imageInfo.height,
                scaleFactor = scaleFactor
            )?.let {
                flip(
                    image = it,
                    isFlipped = imageData.imageInfo.isFlipped
                )
            }?.let {
                onImageReadyToCompressInterceptor(it)
            } ?: return@withContext ByteArray(0)

        return@withContext ImageCompressor.compress(
            image = currentImage,
            imageFormat = imageData.imageInfo.imageFormat,
            quality = imageData.imageInfo.quality
        )
    }

    override suspend fun createPreview(
        image: Bitmap,
        imageInfo: ImageInfo,
        transformations: List<Transformation<Bitmap>>,
        onGetByteCount: (Int) -> Unit
    ): Bitmap = withContext(Dispatchers.IO) {
        if (imageInfo.height == 0 || imageInfo.width == 0) return@withContext image
        var width = imageInfo.width
        var height = imageInfo.height

        launch {
            if (imageInfo.resizeType !is ResizeType.CenterCrop) {
                compress(
                    imageData = ImageData(
                        image = image,
                        imageInfo = imageInfo
                    ),
                    onImageReadyToCompressInterceptor = {
                        transform(
                            image = it,
                            transformations = transformations
                        ) ?: it
                    }
                )
            } else {
                compressCenterCrop(
                    scaleFactor = 1f,
                    onImageReadyToCompressInterceptor = {
                        transform(
                            image = it,
                            transformations = transformations
                        ) ?: it
                    },
                    imageData = ImageData(
                        image = image,
                        imageInfo = imageInfo
                    )
                )
            }.let { onGetByteCount(it.size) }
        }

        var scaleFactor = 1f
        while (!canShow(size = height * width * 4)) {
            height = (height * 0.85f).roundToInt()
            width = (width * 0.85f).roundToInt()
            scaleFactor *= 0.85f
        }

        val bytes = if (imageInfo.resizeType !is ResizeType.CenterCrop) {
            compress(
                imageData = ImageData(
                    image = image,
                    imageInfo = imageInfo.copy(
                        width = width,
                        height = height
                    )
                ),
                onImageReadyToCompressInterceptor = {
                    transform(
                        image = it,
                        transformations = transformations
                    ) ?: it
                }
            )
        } else {
            compressCenterCrop(
                scaleFactor = scaleFactor,
                onImageReadyToCompressInterceptor = {
                    transform(
                        image = it,
                        transformations = transformations
                    ) ?: it
                },
                imageData = ImageData(
                    image = image,
                    imageInfo = imageInfo.copy(
                        width = width,
                        height = height
                    )
                )
            )
        }
        val bitmap = imageLoader.execute(
            ImageRequest.Builder(context).data(bytes).build()
        ).drawable?.toBitmap()
        return@withContext bitmap ?: image
    }

    override suspend fun convertImagesToPdf(
        imageUris: List<String>,
        onProgressChange: suspend (Int) -> Unit,
        scaleSmallImagesToLarge: Boolean
    ): ByteArray = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()

        val (size, images) = calculateCombinedImageDimensionsAndBitmaps(
            imageUris = imageUris,
            combiningParams = CombiningParams(
                scaleSmallImagesToLarge = scaleSmallImagesToLarge,
                isHorizontal = false
            )
        )

        val bitmaps = images.map { image ->
            if (scaleSmallImagesToLarge && image.shouldUpscale(false, size)) {
                image.upscale(false, size)
            } else image
        }

        bitmaps.forEachIndexed { index, imageBitmap ->
            val pageInfo = PdfDocument.PageInfo.Builder(
                imageBitmap.width,
                imageBitmap.height,
                index
            ).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawBitmap(
                imageBitmap,
                0f, 0f,
                Paint().apply {
                    isAntiAlias = true
                }
            )
            pdfDocument.finishPage(page)
            onProgressChange(index)
        }

        val out = ByteArrayOutputStream()
        pdfDocument.writeTo(out)

        return@withContext out.toByteArray().also {
            out.flush()
            out.close()
        }
    }

    override fun convertPdfToImages(
        pdfUri: String,
        pages: List<Int>?,
        preset: Preset.Numeric,
        onGetPagesCount: suspend (Int) -> Unit,
        onProgressChange: suspend (Int, String) -> Unit,
        onComplete: suspend () -> Unit
    ) = CoroutineScope(Dispatchers.Main).launch {
        withContext(Dispatchers.IO) {
            context.contentResolver.openFileDescriptor(
                pdfUri.toUri(),
                "r"
            )?.use { fileDescriptor ->
                val pdfRenderer = PdfRenderer(fileDescriptor)

                onGetPagesCount(pages?.size ?: pdfRenderer.pageCount)

                for (pageIndex in 0 until pdfRenderer.pageCount) {
                    if (pages == null || pages.contains(pageIndex)) {

                        val page = pdfRenderer.openPage(pageIndex)
                        val bitmap = scaleUntilCanShow(
                            Bitmap.createBitmap(
                                (page.width * (preset.value / 100f)).roundToInt(),
                                (page.height * (preset.value / 100f)).roundToInt(),
                                Bitmap.Config.ARGB_8888
                            )
                        )!!
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()

                        val renderedBitmap =
                            Bitmap.createBitmap(
                                bitmap.width,
                                bitmap.height,
                                Bitmap.Config.ARGB_8888
                            )
                        Canvas(renderedBitmap).apply {
                            drawColor(Color.White.toArgb())
                            drawBitmap(bitmap, 0f, 0f, Paint().apply { isAntiAlias = true })
                        }

                        cacheImage(
                            image = renderedBitmap,
                            imageInfo = ImageInfo(
                                width = renderedBitmap.width,
                                height = renderedBitmap.height,
                                imageFormat = ImageFormat.Heic
                            ),
                            name = "image_$pageIndex"
                        )?.let { onProgressChange(pageIndex, it) }
                    }
                }
                onComplete()
                pdfRenderer.close()
            }
        }
    }

    private fun Drawable.toBitmap(): Bitmap? {
        val drawable = this
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getExtension(uri: String): String? {
        if (uri.endsWith(".jxl")) return "jxl"
        return if (ContentResolver.SCHEME_CONTENT == uri.toUri().scheme) {
            MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(
                    context.contentResolver.getType(uri.toUri())
                )
        } else {
            MimeTypeMap.getFileExtensionFromUrl(uri).lowercase(Locale.getDefault())
        }
    }

    private suspend fun ResizeType.CenterCrop.resizeWithCenterCrop(
        image: Bitmap,
        w: Int,
        h: Int,
        scaleFactor: Float = 1f
    ): Bitmap {
        if (w == image.width && h == image.height) return image
        val bitmap = transform(
            image = image.let {
                val xScale: Float = w.toFloat() / it.width
                val yScale: Float = h.toFloat() / it.height
                val scale = xScale.coerceAtLeast(yScale)
                it.createScaledBitmap(
                    width = (scale * it.width).toInt(),
                    height = (scale * it.height).toInt()
                )
            },
            transformations = listOf(
                StackBlurFilter(
                    value = 0.5f to blurRadius
                )
            )
        )
        val drawImage = image.createScaledBitmap(
            width = (image.width * scaleFactor).toInt(),
            height = (image.height * scaleFactor).toInt()
        )
        val canvas = Bitmap.createBitmap(w, h, drawImage.config).apply { setHasAlpha(true) }
        Canvas(canvas).apply {
            drawColor(Color.Transparent.toArgb(), PorterDuff.Mode.CLEAR)
            this@resizeWithCenterCrop.canvasColor?.let {
                drawColor(it)
            } ?: bitmap?.let {
                drawBitmap(
                    bitmap,
                    (width - bitmap.width) / 2f,
                    (height - bitmap.height) / 2f,
                    null
                )
            }
            drawBitmap(
                drawImage,
                (width - drawImage.width) / 2f,
                (height - drawImage.height) / 2f,
                Paint()
            )
        }
        return canvas
    }

    private fun Bitmap.size(): Int {
        return width * height * (if (config == Bitmap.Config.RGB_565) 2 else 4)
    }

    override suspend fun shareUri(uri: String, type: String?) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri.toUri())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.type = type ?: MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(
                    getExtension(uri)
                ) ?: "*/*"
        }
        val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.share))
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    override suspend fun shareImageUris(
        uris: List<String>
    ) = shareImageUris(uris.map { it.toUri() })

    override suspend fun getPdfPages(uri: String): List<Int> {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openFileDescriptor(
                uri.toUri(),
                "r"
            )?.use { fileDescriptor ->
                List(PdfRenderer(fileDescriptor).pageCount) { it }
            } ?: emptyList()
        }
    }

    private val pagesBuf = hashMapOf<String, List<IntegerSize>>()
    override suspend fun getPdfPageSizes(uri: String): List<IntegerSize> {
        return withContext(Dispatchers.IO) {
            if (!pagesBuf[uri].isNullOrEmpty()) {
                pagesBuf[uri]!!
            } else {
                context.contentResolver.openFileDescriptor(
                    uri.toUri(),
                    "r"
                )?.use { fileDescriptor ->
                    val r = PdfRenderer(fileDescriptor)
                    List(r.pageCount) {
                        val page = r.openPage(it)
                        page?.run {
                            IntegerSize(width, height)
                        }.also {
                            page.close()
                        }
                    }.filterNotNull().also {
                        pagesBuf[uri] = it
                    }
                } ?: emptyList()
            }
        }
    }

    private fun shareImageUris(uris: List<Uri>) {
        val sendIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            type = "image/*"
        }
        val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.share))
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    private fun Bitmap.shouldUpscale(
        isHorizontal: Boolean,
        size: IntegerSize
    ): Boolean {
        return if (isHorizontal) this.height != size.height
        else this.width != size.width
    }

    private fun Bitmap.upscale(
        isHorizontal: Boolean,
        size: IntegerSize
    ): Bitmap {
        return if (isHorizontal) {
            createScaledBitmap(
                width = (size.height * aspectRatio).toInt(),
                height = size.height
            )
        } else {
            createScaledBitmap(
                width = size.width,
                height = (size.width / aspectRatio).toInt()
            )
        }
    }

    private fun Bitmap.createScaledBitmap(
        width: Int,
        height: Int
    ): Bitmap {
        if (width == this.width && height == this.height) return this

        return if (width < this.width && height < this.height) {
            BitmapCompat.createScaledBitmap(
                this,
                width,
                height,
                null,
                true
            )
        } else {
            Bitmap.createScaledBitmap(
                this, width, height, true
            )
        }
    }
}