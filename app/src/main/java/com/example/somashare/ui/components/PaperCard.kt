package com.example.somashare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.somashare.data.model.PastPaper

@Composable
fun PaperCard(
    paper: PastPaper,
    onView: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Badge
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            paper.unitCode,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        paper.paperName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(4.dp))

                    Row {
                        // Rating
                        repeat(5) { index ->
                            Icon(
                                if (index < paper.averageRating) Icons.Filled.Star
                                else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("(${paper.ratingCount})", fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("${paper.downloadCount} downloads", fontSize = 12.sp)
                    }
                }

                Column {
                    IconButton(onClick = onView) {
                        Icon(Icons.Default.Visibility, "View")
                    }
                    IconButton(onClick = onDownload) {
                        Icon(Icons.Default.Download, "Download")
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            }
        }
    }
}