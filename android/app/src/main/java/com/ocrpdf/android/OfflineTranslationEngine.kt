package com.ocrpdf.android

/**
 * Contract for a fully offline English-to-Vietnamese model.
 * Implementations should tokenize and translate small chunks to bound memory use.
 */
interface OfflineTranslationEngine {
    suspend fun translateBatch(
        texts: List<String>,
        sourceLanguage: String = "en",
        targetLanguage: String = "vi"
    ): List<String>

    fun close()
}

/** Safe default until a real model and tokenizer are bundled and validated. */
class TranslationModelNotInstalledException : IllegalStateException(
    "No offline translation model is installed. Add and validate a compatible model before exporting."
)

class MissingTranslationEngine : OfflineTranslationEngine {
    override suspend fun translateBatch(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String
    ): List<String> = throw TranslationModelNotInstalledException()

    override fun close() = Unit
}
