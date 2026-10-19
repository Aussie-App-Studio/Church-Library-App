package com.churchbooklibrary.feature.returns

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import com.churchbooklibrary.feature.borrow.BookCoverImage
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.churchbooklibrary.R
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.churchbooklibrary.core.database.LoanWithBook
import com.churchbooklibrary.theme.Green100
import com.churchbooklibrary.theme.Green600
import com.churchbooklibrary.theme.Green700
import com.churchbooklibrary.theme.Red100
import com.churchbooklibrary.theme.Red600
import com.churchbooklibrary.theme.Slate200
import com.churchbooklibrary.theme.Slate400
import com.churchbooklibrary.theme.Slate600

data class BorrowerGroup(val name: String, val email: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnBooksScreen(
    viewModel: ReturnViewModel,
    onBack: () -> Unit
) {
    val loans by viewModel.activeLoans.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val returnedLoanId by viewModel.returnedLoanId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var confirmingLoan by remember { mutableStateOf<LoanWithBook?>(null) }
    var confirmingReturnAllBorrower by remember { mutableStateOf<String?>(null) }
    var confirmingReturnAllLoans by remember { mutableStateOf<List<LoanWithBook>?>(null) }

    LaunchedEffect(returnedLoanId) {
        returnedLoanId?.let {
            if (it == -1L) {
                snackbarHostState.showSnackbar(context.getString(R.string.all_books_returned_success))
            } else {
                snackbarHostState.showSnackbar(context.getString(R.string.book_returned_success))
            }
            viewModel.clearReturnedEvent()
        }
    }

    if (confirmingLoan != null) {
        AlertDialog(
            onDismissRequest = { confirmingLoan = null },
            title = { Text(stringResource(R.string.return_book_dialog_title)) },
            text = {
                Text(stringResource(R.string.return_book_dialog_message, confirmingLoan!!.book.title, confirmingLoan!!.loan.borrowerName))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.returnBook(confirmingLoan!!.loan.id)
                        confirmingLoan = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green700)
                ) { Text(stringResource(R.string.return_book_button)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingLoan = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (confirmingReturnAllBorrower != null && confirmingReturnAllLoans != null) {
        AlertDialog(
            onDismissRequest = { 
                confirmingReturnAllBorrower = null 
                confirmingReturnAllLoans = null
            },
            title = { Text(stringResource(R.string.return_all_dialog_title)) },
            text = {
                Text(stringResource(R.string.return_all_dialog_message, confirmingReturnAllLoans!!.size, confirmingReturnAllBorrower!!))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.returnAllBooksForBorrower(confirmingReturnAllBorrower!!, confirmingReturnAllLoans!!.map { it.loan.id })
                        confirmingReturnAllBorrower = null
                        confirmingReturnAllLoans = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green700)
                ) { Text(stringResource(R.string.return_all_button)) }
            },
            dismissButton = {
                TextButton(onClick = { 
                    confirmingReturnAllBorrower = null 
                    confirmingReturnAllLoans = null
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.return_books_title)) },
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_return_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (loans.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AssignmentReturn,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(stringResource(R.string.no_active_loans), color = Slate400)
                    }
                }
            } else {
                val groupedLoans = loans.groupBy { BorrowerGroup(it.loan.borrowerName, it.loan.borrowerEmail) }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    groupedLoans.forEach { (borrowerGroup, borrowerLoans) ->
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = borrowerGroup.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (borrowerGroup.email.isNotBlank()) {
                                        Text(
                                            text = borrowerGroup.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate600
                                        )
                                    }
                                }
                                Button(
                                    onClick = { 
                                        confirmingReturnAllBorrower = if (borrowerGroup.email.isNotBlank()) "${borrowerGroup.name} (${borrowerGroup.email})" else borrowerGroup.name
                                        confirmingReturnAllLoans = borrowerLoans
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Green600)
                                ) {
                                    Text(stringResource(R.string.return_all_count, borrowerLoans.size))
                                }
                            }
                        }
                        items(borrowerLoans, key = { it.loan.id }) { loanWithBook ->
                            LoanCard(
                                loanWithBook = loanWithBook,
                                onReturn = { confirmingLoan = loanWithBook }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanCard(
    loanWithBook: LoanWithBook,
    onReturn: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isOverdue = loanWithBook.loan.dueDate < now
    val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    val dueDateText = dateFormat.format(Date(loanWithBook.loan.dueDate))

    val borderColor = if (isOverdue) Red600 else Slate200
    val bgColor = if (isOverdue) Red100 else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCoverImage(
                title = loanWithBook.book.title,
                author = loanWithBook.book.author,
                coverUrl = loanWithBook.book.coverUrl,
                modifier = Modifier
                    .size(width = 80.dp, height = 120.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = loanWithBook.book.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = loanWithBook.book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Spacer(Modifier.height(12.dp))
                val borrowerDisplay = if (loanWithBook.loan.borrowerEmail.isNotBlank()) {
                    "${loanWithBook.loan.borrowerName} (${loanWithBook.loan.borrowerEmail})"
                } else {
                    loanWithBook.loan.borrowerName
                }
                Text(
                    text = stringResource(R.string.borrower_label, borrowerDisplay),
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                if (isOverdue) {
                    Surface(
                        color = Red600,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.overdue_badge),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.due_date_label, dueDateText),
                        style = MaterialTheme.typography.bodySmall,
                        color = Red600,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(R.string.due_date_label, dueDateText),
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onReturn,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green600),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.return_book_button), fontSize = 14.sp)
                }
            }
        }
    }
}



