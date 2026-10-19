package com.churchbooklibrary.feature.borrow

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.churchbooklibrary.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.churchbooklibrary.theme.Blue600
import com.churchbooklibrary.theme.Green100
import com.churchbooklibrary.theme.Green600
import com.churchbooklibrary.theme.Green700
import com.churchbooklibrary.theme.Slate600
import com.churchbooklibrary.theme.Slate50
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BorrowSuccessScreen(
    viewModel: BorrowViewModel,
    onHome: () -> Unit
) {
    val borrowSuccess by viewModel.borrowSuccess.collectAsStateWithLifecycle()

    // Auto-navigate home after 10 seconds
    LaunchedEffect(Unit) {
        delay(10_000)
        viewModel.clearSuccess()
        onHome()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    val dueDateText = borrowSuccess?.let {
        dateFormat.format(Date(it.dueDate))
    } ?: ""
    val bookCount = borrowSuccess?.bookCount ?: 0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Slate50
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Green100,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.success),
                        tint = Green600,
                        modifier = Modifier.padding(28.dp)
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.success_title),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Green700
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (bookCount == 1) {
                        stringResource(R.string.success_one_book, dueDateText)
                    } else {
                        stringResource(R.string.success_multiple_books, bookCount, dueDateText)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Slate600,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.returning_home_seconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.clearSuccess()
                        onHome()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.widthIn(min = 260.dp)
                ) {
                    Text(
                        stringResource(R.string.return_to_home_button),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

