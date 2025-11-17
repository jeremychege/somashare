package com.example.somashare.ui.pdfviewer

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.somashare.data.remote.FirebaseStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class PdfViewerUiState(
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val loadingProgress: Int = 0,
    val pdfFile: File? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 0,
    val errorMessage: String? = null
)

class PdfViewerViewModel : ViewModel() {

    private val storageService = FirebaseStorageService()

    private val _uiState = MutableStateFlow(PdfViewerUiState())
    val uiState: StateFlow<PdfViewerUiState> = _uiState.asStateFlow()

    fun loadPdf(context: Context, downloadUrl: String, paperId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoaded = false,
                    loadingProgress = 0,
                    errorMessage = null
                )
            }

            try {
                val cacheDir = File(context.cacheDir, "pdfs")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                val fileName = "paper_$paperId.pdf"
                val file = File(cacheDir, fileName)

                if (file.exists()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoaded = true,
                            pdfFile = file
                        )
                    }
                } else {
                    val downloadedFile = downloadPdfFile(downloadUrl, file) { progress ->
                        _uiState.update { it.copy(loadingProgress = progress) }
                    }

                    if (downloadedFile != null) {
                        storageService.incrementDownloadCount(paperId)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoaded = true,
                                loadingProgress = 100,
                                pdfFile = downloadedFile
                            )
                        }
                    } else {
                        throw Exception("Failed to download PDF")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoaded = false,
                        errorMessage = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    private suspend fun downloadPdfFile(
        url: String,
        destinationFile: File,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }

            val fileLength = connection.contentLength
            val input = connection.inputStream
            val output = FileOutputStream(destinationFile)

            val buffer = ByteArray(4096)
            var total: Long = 0
            var count: Int

            while (input.read(buffer).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    val progress = (total * 100 / fileLength).toInt()
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }
                output.write(buffer, 0, count)
            }

            output.flush()
            output.close()
            input.close()

            destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateCurrentPage(page: Int, total: Int) {
        _uiState.update {
            it.copy(
                currentPage = page,
                totalPages = total
            )
        }
    }

    fun sharePdf(context: Context) {
        val file = _uiState.value.pdfFile ?: return

        viewModelScope.launch {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}