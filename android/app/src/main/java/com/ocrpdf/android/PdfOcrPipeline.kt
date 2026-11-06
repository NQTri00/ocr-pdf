package com.ocrpdf.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class TextRegion(val text: String, val bounds: Rect)
data class PageOcrResult(
    val pageNumber: Int,
    val width: Int,
    val height: Int,
    val fullText: String,
    val regions: List<TextRegion>
)
data class InspectionSummary(
    val pageCount: Int,
    val regionCount: Int,
    val textPreview: String,
    val reportFile: File
)

/** Opens the source read-only and processes one rendered page at a time. */
class PdfOcrPipeline(
    private val context: Context,
    private val renderDpi: Int = 150
) {
    init {
        require(renderDpi in 100..200) { "Mobile render DPI must be between 100 and 200." }
    }

    fun inspect(uri: Uri, onProgress: (page: Int, total: Int) -> Unit = { _, _ -> }): InspectionSummary {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: error("The selected PDF could not be opened.")

        descriptor.use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                require(renderer.pageCount > 0) { "The PDF contains no pages." }
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                try {
                    val pages = JSONArray()
                    var totalRegions = 0
                    val preview = StringBuilder()

                    for (index in 0 until renderer.pageCount) {
                        onProgress(index + 1, renderer.pageCount)
                        val pageResult = renderer.openPage(index).use { page ->
                            val scale = renderDpi / 72f
                            val requestedWidth = (page.width * scale).toInt().coerceAtLeast(1)
                            val requestedHeight = (page.height * scale).toInt().coerceAtLeast(1)
                            val limitScale = minOf(
                                1f,
                                2600f / requestedWidth,
                                2600f / requestedHeight
                            )
                            val width = (requestedWidth * limitScale).toInt().coerceAtLeast(1)
                            val height = (requestedHeight * limitScale).toInt().coerceAtLeast(1)
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            try {
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                recognizePage(index + 1, bitmap, recognizer)
                            } finally {
                                bitmap.recycle()
                            }
                        }

                        totalRegions += pageResult.regions.size
                        if (preview.length < 8_000 && pageResult.fullText.isNotBlank()) {
                            preview.appendLine("--- Page ${pageResult.pageNumber} ---")
                            preview.appendLine(pageResult.fullText.take(2_000))
                        }
                        pages.put(pageToJson(pageResult))
                    }

                    val report = JSONObject()
                        .put("schemaVersion", 1)
                        .put("sourceUri", uri.toString())
                        .put("renderDpi", renderDpi)
                        .put("pageCount", renderer.pageCount)
                        .put("regionCount", totalRegions)
                        .put("pages", pages)

                    val directory = File(context.filesDir, "reports").apply { mkdirs() }
                    val reportFile = File(directory, "ocr-report.json")
                    FileOutputStream(reportFile).use {
                        it.write(report.toString(2).toByteArray(Charsets.UTF_8))
                    }
                    return InspectionSummary(renderer.pageCount, totalRegions, preview.toString(), reportFile)
                } finally {
                    recognizer.close()
                }
            }
        }
    }

    private fun recognizePage(
        pageNumber: Int,
        bitmap: Bitmap,
        recognizer: TextRecognizer
    ): PageOcrResult {
        val result = Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0)))
        val regions = result.textBlocks.mapNotNull { block ->
            val box = block.boundingBox ?: return@mapNotNull null
            TextRegion(block.text, Rect(box))
        }
        return PageOcrResult(pageNumber, bitmap.width, bitmap.height, result.text, regions)
    }

    private fun pageToJson(page: PageOcrResult): JSONObject {
        val regions = JSONArray()
        page.regions.forEach { region ->
            regions.put(
                JSONObject()
                    .put("text", region.text)
                    .put("left", region.bounds.left)
                    .put("top", region.bounds.top)
                    .put("right", region.bounds.right)
                    .put("bottom", region.bounds.bottom)
            )
        }
        return JSONObject()
            .put("pageNumber", page.pageNumber)
            .put("width", page.width)
            .put("height", page.height)
            .put("text", page.fullText)
            .put("regions", regions)
    }
}
