package com.churchbooklibrary.feature.borrow

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.churchbooklibrary.theme.Blue600
import com.churchbooklibrary.theme.Green600
import com.churchbooklibrary.theme.Green700
import com.churchbooklibrary.theme.Slate600
import com.churchbooklibrary.theme.Slate700

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.churchbooklibrary.theme.Blue50
import com.churchbooklibrary.theme.Blue800
import com.churchbooklibrary.theme.Red100
import com.churchbooklibrary.theme.Red600
import com.churchbooklibrary.R
import com.churchbooklibrary.core.datastore.BorrowPeriod
import com.churchbooklibrary.core.datastore.BorrowPeriodUnit
import androidx.compose.ui.res.pluralStringResource

/**
 * Human-readable label for a borrow period. The four factory defaults use the existing
 * fully-translated strings; custom periods use English plurals (with default-locale fallback).
 */
@Composable
fun borrowPeriodLabel(period: BorrowPeriod): String {
    val defaultRes = when (period.value) {
        "1w" -> R.string.duration_1_week
        "2w" -> R.string.duration_2_weeks
        "1m" -> R.string.duration_1_month
        "2m" -> R.string.duration_2_months
        else -> null
    }
    if (defaultRes != null) return stringResource(defaultRes)
    val pluralsRes = when (period.unit) {
        BorrowPeriodUnit.DAYS -> R.plurals.borrow_period_days
        BorrowPeriodUnit.WEEKS -> R.plurals.borrow_period_weeks
        BorrowPeriodUnit.MONTHS -> R.plurals.borrow_period_months
    }
    return pluralStringResource(pluralsRes, period.count, period.count)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerInfoScreen(
    viewModel: BorrowViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val borrowPeriods by viewModel.borrowPeriods.collectAsStateWithLifecycle()
    var selectedDurationValue by remember { mutableStateOf<String?>(null) }
    val selectedPeriod = borrowPeriods.firstOrNull { it.value == selectedDurationValue } ?: borrowPeriods[0]
    var showCalendarPrompt by remember { mutableStateOf(false) }
    var autofilledEmail by remember { mutableStateOf("") }
    var autofilledPhone by remember { mutableStateOf("") }
    var autofilledName by remember { mutableStateOf("") }

    LaunchedEffect(name) {
        val trimmedName = name.trim()
        if (trimmedName.length < 5) {
            if (email == autofilledEmail) {
                email = ""
                autofilledEmail = ""
            }
            if (phone == autofilledPhone) {
                phone = ""
                autofilledPhone = ""
            }
        } else {
            val contactInfo = viewModel.lookupBorrowerContact(trimmedName)
            if (contactInfo != null) {
                if (email.isBlank() || email == autofilledEmail) {
                    email = contactInfo.first
                    autofilledEmail = contactInfo.first
                }
                if (phone.isBlank() || phone == autofilledPhone) {
                    phone = contactInfo.second
                    autofilledPhone = contactInfo.second
                }
            } else {
                if (email == autofilledEmail) {
                    email = ""
                    autofilledEmail = ""
                }
                if (phone == autofilledPhone) {
                    phone = ""
                    autofilledPhone = ""
                }
            }
        }
    }

    LaunchedEffect(email) {
        val trimmedEmail = email.trim()
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            val contactInfo = viewModel.lookupBorrowerContactByEmail(trimmedEmail)
            if (contactInfo != null) {
                if (name.isBlank() || name == autofilledName) {
                    name = contactInfo.first
                    autofilledName = contactInfo.first
                }
                if (phone.isBlank() || phone == autofilledPhone) {
                    phone = contactInfo.second
                    autofilledPhone = contactInfo.second
                }
            }
        }
    }

    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val borrowSuccess by viewModel.borrowSuccess.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val books by viewModel.books.collectAsStateWithLifecycle()
    val selectedBooksMap by viewModel.selectedBooks.collectAsStateWithLifecycle()
    val selectedBooks = books.filter { it.book.id in selectedBooksMap.keys }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it.asString(context))
            viewModel.clearError()
        }
    }

    LaunchedEffect(name, email, phone) {
        viewModel.updateBorrowerContact(name, email, phone)
    }

    val borrowerActiveLoans by viewModel.borrowerActiveLoans.collectAsStateWithLifecycle()
    val maxBorrowLimit by viewModel.maxBorrowLimit.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var isCheckingEligibility by remember { mutableStateOf(false) }

    LaunchedEffect(borrowSuccess) {
        if (borrowSuccess != null) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.checkout_details_title)) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 700.dp)
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.your_details),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.your_details_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.full_name_required)) },
                            placeholder = { Text(stringResource(R.string.full_name_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text(stringResource(R.string.email_required)) },
                                placeholder = { Text(stringResource(R.string.email_placeholder)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                                )
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(stringResource(R.string.phone_optional)) },
                                placeholder = { Text(stringResource(R.string.phone_placeholder)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                                )
                            )
                        }

                        if (borrowerActiveLoans.isNotEmpty()) {
                            val now = System.currentTimeMillis()
                            val overdueLoans = borrowerActiveLoans.filter { it.loan.dueDate < now }
                            val activeCount = borrowerActiveLoans.sumOf { it.loan.quantity.coerceAtLeast(1) }
                            val remainingAllowed = (maxBorrowLimit - activeCount).coerceAtLeast(0)

                            if (overdueLoans.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Red100,
                                    border = BorderStroke(1.dp, Red600),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Red600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = stringResource(R.string.active_loans_overdue_warning, overdueLoans.size),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Red600,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            } else if (activeCount >= maxBorrowLimit) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Red100,
                                    border = BorderStroke(1.dp, Red600),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Red600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = stringResource(R.string.active_loans_limit_reached, activeCount, maxBorrowLimit),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Red600,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Blue50,
                                    border = BorderStroke(1.dp, Blue600),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Blue600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = stringResource(R.string.active_loans_info, activeCount, maxBorrowLimit, remainingAllowed),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Blue800,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Duration selector
                        Text(
                            text = stringResource(R.string.borrow_duration_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = Slate600
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            borrowPeriods.forEach { option ->
                                FilterChip(
                                    selected = selectedPeriod == option,
                                    onClick = { selectedDurationValue = option.value },
                                    label = { Text(borrowPeriodLabel(option), fontSize = 15.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Blue600,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (!isCheckingEligibility) {
                                coroutineScope.launch {
                                    isCheckingEligibility = true
                                    val isEligible = viewModel.validateBorrowEligibility(name, email, phone)
                                    isCheckingEligibility = false
                                    if (isEligible) {
                                        showCalendarPrompt = true
                                    }
                                }
                            }
                        },
                        enabled = !isCheckingEligibility,
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isCheckingEligibility) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Checking…", fontSize = 16.sp)
                        } else {
                            Icon(androidx.compose.material.icons.Icons.Default.DateRange, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Text(stringResource(R.string.review_and_confirm), fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(Modifier.height(48.dp))

                if (selectedBooks.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.books_to_checkout, selectedBooks.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate700,
                        modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        selectedBooks.forEach { item ->
                            val book = item.book
                            val qty = selectedBooksMap[book.id] ?: 1
                            Column(
                                modifier = Modifier.width(120.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                BookCoverImage(
                                    title = book.title,
                                    author = book.author,
                                    coverUrl = book.coverUrl,
                                    modifier = Modifier
                                        .size(100.dp, 150.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = book.author,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showCalendarPrompt) {
        val durationLabel = borrowPeriodLabel(selectedPeriod)
        CalendarReminderDialog(
            durationLabel = durationLabel,
            onConfirm = {
                showCalendarPrompt = false
                viewModel.confirmBorrow(name, email, phone, selectedPeriod.value, durationLabel)
            }
        )
    }
}

@Composable
fun CalendarReminderDialog(
    durationLabel: String,
    onConfirm: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(10) }
    
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            kotlinx.coroutines.delay(1000)
            timeLeft--
        }
        onConfirm()
    }

    AlertDialog(
        onDismissRequest = { /* Forced to wait or confirm */ },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(androidx.compose.material.icons.Icons.Default.DateRange, contentDescription = null, tint = Blue600)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.reminder_dialog_title))
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.reminder_dialog_message, durationLabel),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.reminder_dialog_offline_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Green700)
            ) {
                Text(if (timeLeft > 0) stringResource(R.string.confirm_loan_timer, timeLeft) else stringResource(R.string.confirm_loan))
            }
        }
    )
}


