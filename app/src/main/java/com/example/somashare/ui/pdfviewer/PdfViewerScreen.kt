//package com.example.somashare.ui.pdfviewer
//
//import android.app.DownloadManager
//import android.content.Context
//import android.net.Uri
//import android.os.Environment
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.viewmodel.compose.viewModel
////import com.github.barteksc.pdfviewer.PDFView
////import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
////import com.github.barteksc.pdfviewer.util.FitPolicy
//import java.io.File
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PdfViewerScreen(
//    paperId: String,
//    paperName: String,
//    downloadUrl: String,
//    onNavigateBack: () -> Unit,
//    viewModel: PdfViewerViewModel = viewModel()
//) {
//    val context = LocalContext.current
//    val uiState by viewModel.uiState.collectAsState()
//    var showMenu by remember { mutableStateOf(false) }
//
//    LaunchedEffect(downloadUrl) {
//        viewModel.loadPdf(context, downloadUrl, paperId)
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(text = paperName, maxLines = 1) },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.Default.ArrowBack, "Back")
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { showMenu = true }) {
//                        Icon(Icons.Default.MoreVert, "Menu")
//                    }
//
//                    DropdownMenu(
//                        expanded = showMenu,
//                        onDismissRequest = { showMenu = false }
//                    ) {
//                        DropdownMenuItem(
//                            text = { Text("Download") },
//                            onClick = {
//                                downloadPdf(context, downloadUrl, paperName)
//                                showMenu = false
//                            },
//                            leadingIcon = { Icon(Icons.Default.Download, null) }
//                        )
//                        DropdownMenuItem(
//                            text = { Text("Share") },
//                            onClick = {
//                                viewModel.sharePdf(context)
//                                showMenu = false
//                            },
//                            leadingIcon = { Icon(Icons.Default.Share, null) }
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
//                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
//                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
//                )
//            )
//        },
//        bottomBar = {
//            if (uiState.isLoaded) {
//                BottomAppBar(
//                    containerColor = MaterialTheme.colorScheme.surfaceVariant
//                ) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text("Page ${uiState.currentPage} / ${uiState.totalPages}")
//                    }
//                }
//            }
//        }
//    ) { padding ->
//        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
//            when {
//                uiState.isLoading -> {
//                    Column(
//                        modifier = Modifier.fillMaxSize(),
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        CircularProgressIndicator()
//                        Spacer(Modifier.height(16.dp))
//                        Text("Loading PDF...")
//                        if (uiState.loadingProgress > 0) {
//                            Spacer(Modifier.height(8.dp))
//                            LinearProgressIndicator(
//                                progress = { uiState.loadingProgress / 100f },
//                                modifier = Modifier.width(200.dp)
//                            )
//                            Text("${uiState.loadingProgress}%")
//                        }
//                    }
//                }
//
//                uiState.errorMessage != null -> {
//                    Column(
//                        modifier = Modifier.fillMaxSize().padding(32.dp),
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Icon(Icons.Default.Error, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
//                        Spacer(Modifier.height(16.dp))
//                        Text("Error Loading PDF", style = MaterialTheme.typography.titleLarge)
//                        Spacer(Modifier.height(8.dp))
//                        Text(uiState.errorMessage ?: "")
//                        Spacer(Modifier.height(24.dp))
//                        Button(onClick = { viewModel.loadPdf(context, downloadUrl, paperId) }) {
//                            Icon(Icons.Default.Refresh, null)
//                            Spacer(Modifier.width(8.dp))
//                            Text("Retry")
//                        }
//                    }
//                }
//
//                uiState.isLoaded && uiState.pdfFile != null -> {
//                    PdfViewer(
//                        file = uiState.pdfFile!!,
//                        onPageChanged = { page, total -> viewModel.updateCurrentPage(page, total) }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun PdfViewer(file: File, onPageChanged: (Int, Int) -> Unit) {
//    AndroidView(
//        factory = { context ->
//            PDFView(context, null).apply {
//                fromFile(file)
//                    .enableSwipe(true)
//                    .swipeHorizontal(false)
//                    .enableDoubletap(true)
//                    .defaultPage(0)
//                    .enableAnnotationRendering(true)
//                    .scrollHandle(DefaultScrollHandle(context))
//                    .enableAntialiasing(true)
//                    .spacing(10)
//                    .pageFitPolicy(FitPolicy.WIDTH)
//                    .onPageChange { page, pageCount ->
//                        onPageChanged(page + 1, pageCount)
//                    }
//                    .load()
//            }
//        },
//        modifier = Modifier.fillMaxSize()
//    )
//}
//
//private fun downloadPdf(context: Context, url: String, fileName: String) {
//    val request = DownloadManager.Request(Uri.parse(url))
//        .setTitle(fileName)
//        .setDescription("Downloading PDF")
//        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
//
//    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//    downloadManager.enqueue(request)
//}