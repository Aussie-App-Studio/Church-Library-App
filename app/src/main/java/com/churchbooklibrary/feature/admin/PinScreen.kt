package com.churchbooklibrary.feature.admin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.churchbooklibrary.R
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.churchbooklibrary.theme.Blue600
import com.churchbooklibrary.theme.DarkSurface
import com.churchbooklibrary.theme.DarkSurface2
import com.churchbooklibrary.theme.Slate400

@Composable
fun PinScreen(
    viewModel: PinViewModel,
    onAuthenticated: () -> Unit,
    onBack: () -> Unit
) {
    val enteredPin by viewModel.enteredPin.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val isDefaultPin by viewModel.isDefaultPin.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is PinViewModel.AuthState.Authenticated) {
            onAuthenticated()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkSurface2
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Close button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lock icon
                Surface(
                    shape = CircleShape,
                    color = Blue600.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Blue600,
                        modifier = Modifier
                            .padding(20.dp)
                            .size(40.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.admin_access_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.enter_pin_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400
                )
                if (isDefaultPin) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Blue600.copy(alpha = 0.15f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.default_pin_hint),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.default_pin_change_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // PIN dots — show 4 dots (hardcoded master pin extends invisibly)
                val isError = authState is PinViewModel.AuthState.Error
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    (1..4).forEach { i ->
                        val filled = i <= enteredPin.length
                        val dotColor by animateColorAsState(
                            targetValue = when {
                                isError -> Color(0xFFEF4444)
                                filled -> Blue600
                                else -> Color(0xFF334155)
                            },
                            animationSpec = tween(200),
                            label = "dot_color_$i"
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                if (isError) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.incorrect_pin_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF4444)
                    )
                }

                Spacer(Modifier.height(40.dp))

                // Numpad
                val digits = listOf(
                    listOf('1', '2', '3'),
                    listOf('4', '5', '6'),
                    listOf('7', '8', '9')
                )
                digits.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        row.forEach { digit ->
                            NumpadButton(label = digit.toString()) { viewModel.onDigit(digit) }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Spacer(Modifier.size(88.dp)) // placeholder for alignment
                    NumpadButton(label = "0") { viewModel.onDigit('0') }
                    FilledTonalIconButton(
                        onClick = { viewModel.onBackspace() },
                        modifier = Modifier.size(88.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = DarkSurface
                        )
                    ) {
                        Icon(
                            Icons.Default.Backspace,
                            contentDescription = stringResource(R.string.delete),
                            tint = Slate400,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumpadButton(label: String, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(88.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = DarkSurface
        )
    ) {
        Text(
            text = label,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

