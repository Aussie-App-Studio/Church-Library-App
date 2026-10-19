package com.churchbooklibrary.feature.home
import androidx.compose.material3.MaterialTheme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import android.content.res.Configuration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.churchbooklibrary.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.io.File
import com.churchbooklibrary.theme.Blue100
import com.churchbooklibrary.theme.Blue600
import com.churchbooklibrary.theme.Green100
import com.churchbooklibrary.theme.Green600
import com.churchbooklibrary.theme.Slate100
import com.churchbooklibrary.theme.Slate50
import com.churchbooklibrary.theme.Slate600

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBorrow: () -> Unit,
    onReturn: () -> Unit,
    onAdmin: () -> Unit
) {
    val libraryName by viewModel.libraryName.collectAsStateWithLifecycle()
    val hasCustomLogo by viewModel.hasCustomLogo.collectAsStateWithLifecycle()
    val logoLastModified by viewModel.logoLastModified.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdmin,
                containerColor = Slate100,
                contentColor = Slate600
            ) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.admin))
            }
        },
        containerColor = Slate50
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var isLogoCorrupted by remember(logoLastModified, hasCustomLogo) { mutableStateOf(false) }
                    val logoFile = remember(logoLastModified) { File(context.filesDir, "logo.png") }
                    val isCustomLogoValid = hasCustomLogo && !isLogoCorrupted && logoFile.exists() && logoFile.length() > 0L

                    if (isCustomLogoValid) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(logoFile)
                                .memoryCacheKey("library_logo_$logoLastModified")
                                .diskCacheKey("library_logo_$logoLastModified")
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.library_logo),
                            contentScale = ContentScale.Crop,
                            onError = {
                                isLogoCorrupted = true
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = libraryName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Welcome text
            Column(
                modifier = Modifier.padding(top = 40.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.welcome_to_library),
                    style = MaterialTheme.typography.displaySmall,
                    color = Slate600,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Slate600.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 500.dp)
                )
            }

            // Action cards
            val configuration = LocalConfiguration.current
            val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

            if (isPortrait) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .widthIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    KioskActionCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.borrow_books),
                        subtitle = stringResource(R.string.borrow_books_desc),
                        icon = Icons.Default.Book,
                        iconBgColor = Blue100,
                        iconColor = Blue600,
                        borderColor = Blue600,
                        onClick = onBorrow
                    )
                    KioskActionCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.return_books),
                        subtitle = stringResource(R.string.return_books_desc),
                        icon = Icons.Default.Book,
                        iconBgColor = Green100,
                        iconColor = Green600,
                        borderColor = Green600,
                        onClick = onReturn
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .widthIn(max = 900.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    KioskActionCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.borrow_books),
                        subtitle = stringResource(R.string.borrow_books_desc),
                        icon = Icons.Default.Book,
                        iconBgColor = Blue100,
                        iconColor = Blue600,
                        borderColor = Blue600,
                        onClick = onBorrow
                    )
                    KioskActionCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.return_books),
                        subtitle = stringResource(R.string.return_books_desc),
                        icon = Icons.Default.Book,
                        iconBgColor = Green100,
                        iconColor = Green600,
                        borderColor = Green600,
                        onClick = onReturn
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KioskActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "card_scale"
    )

    OutlinedCard(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = iconBgColor,
                modifier = Modifier.size(96.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .padding(24.dp)
                        .size(48.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate600,
                textAlign = TextAlign.Center
            )
        }
    }
}





