package com.ocrpdf.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val worker = Executors.newSingleThreadExecutor()
    private var selectedPdf: Uri? = null
    private lateinit var statusText: TextView
    private lateinit var chooseButton: Button
    private lateinit var inspectButton: Button

    private val openPdf = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedPdf = uri
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // A provider may grant only temporary access.
            }
            statusText.text = "Selected PDF:\n$uri\n\nTap Analyze PDF to run offline OCR."
            inspectButton.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusText = TextView(this).apply {
            textSize = 15f
            text = "Choose a PDF to inspect. OCR runs locally on this device."
            setPadding(16, 20, 16, 20)
        }
        chooseButton = Button(this).apply {
            text = "Choose PDF"
            setOnClickListener { openPdf.launch(arrayOf("application/pdf")) }
        }
        inspectButton = Button(this).apply {
            text = "Analyze PDF"
            isEnabled = false
            setOnClickListener { runInspection() }
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 24)
            addView(chooseButton, buttonParams())
            addView(inspectButton, buttonParams())
            addView(statusText, buttonParams())
        }
        setContentView(ScrollView(this).apply { addView(content) })
    }

    private fun buttonParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = 8 }

    private fun runInspection() {
        val uri = selectedPdf ?: return
        chooseButton.isEnabled = false
        inspectButton.isEnabled = false
        statusText.text = "Starting local OCR…"
        worker.execute {
            try {
                val result = PdfOcrPipeline(this).inspect(uri) { page, total ->
                    runOnUiThread { statusText.text = "Recognizing page $page of $total…" }
                }
                runOnUiThread {
                    statusText.text = buildString {
                        appendLine("OCR inspection complete")
                        appendLine("Pages: ${result.pageCount}")
                        appendLine("Text regions: ${result.regionCount}")
                        appendLine("Report: ${result.reportFile.absolutePath}")
                        appendLine()
                        appendLine("Recognized text preview:")
                        append(result.textPreview.ifBlank { "(No text recognized)" })
                        appendLine()
                        appendLine()
                        append("Translation and PDF reconstruction are not implemented yet. ")
                        append("The app will not overlay Vietnamese on top of source English.")
                    }
                    chooseButton.isEnabled = true
                    inspectButton.isEnabled = true
                }
            } catch (error: Exception) {
                runOnUiThread {
                    statusText.text = "Could not process PDF:\n${error.message ?: error.javaClass.simpleName}"
                    chooseButton.isEnabled = true
                    inspectButton.isEnabled = true
                }
            }
        }
    }

    override fun onDestroy() {
        worker.shutdown()
        super.onDestroy()
    }
}
