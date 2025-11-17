
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Standard TopBar with back button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF4F46E5),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

// Home TopBar with greeting and profile
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    greeting: String,
    userName: String,
    userYear: Int,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF4F46E5),
            titleContentColor = Color.White
        ),
        title = {
            Column {
                Text(
                    text = if (userName.isNotEmpty()) {
                        "$greeting, $userName!"
                    } else {
                        greeting
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                if (userName.isNotEmpty()) {
                    Text(
                        text = "Year $userYear Student",
                        fontSize = 14.sp,
                        color = Color(0xFFC7D2FE)
                    )
                }
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6366F1)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    )
}

// TopBar with custom action icon
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithAction(
    title: String,
    onNavigateBack: () -> Unit,
    actionIcon: ImageVector,
    actionContentDescription: String,
    onActionClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(actionIcon, actionContentDescription)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF4F46E5),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}
