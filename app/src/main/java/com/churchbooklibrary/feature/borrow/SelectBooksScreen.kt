package com.churchbooklibrary.feature.borrow
import androidx.compose.material3.MaterialTheme

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.churchbooklibrary.R
import com.churchbooklibrary.core.util.getLocalizedCategoryName
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.foundation.background
import kotlinx.coroutines.delay
import com.churchbooklibrary.core.database.BookEntity
import com.churchbooklibrary.core.database.BookWithAvailability
import com.churchbooklibrary.core.datastore.SettingsDataStore
import com.churchbooklibrary.theme.Blue100
import com.churchbooklibrary.theme.Blue600
import com.churchbooklibrary.theme.Slate100
import com.churchbooklibrary.theme.Slate200
import com.churchbooklibrary.theme.Slate400
import com.churchbooklibrary.theme.Slate600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBooksScreen(
    viewModel: BorrowViewModel,
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    val books by viewModel.books.collectAsStateWithLifecycle()
    val selectedBooks by viewModel.selectedBooks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val categoriesEnabled by viewModel.categoriesEnabled.collectAsStateWithLifecycle()
    val enabledCategories by viewModel.enabledCategories.collectAsStateWithLifecycle()
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()

    val maxBorrowLimit by viewModel.maxBorrowLimit.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var rejectedBookId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it.asString(context))
            viewModel.clearError()
        }
    }

    LaunchedEffect(rejectedBookId) {
        if (rejectedBookId != null) {
            delay(1000)
            rejectedBookId = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_books_title)) },
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
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFB91C1C), // Red 700
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.selected_books_count, selectedBooks.size, maxBorrowLimit),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Slate600,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Button(
                            onClick = onProceed,
                            enabled = selectedBooks.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                        ) {
                            Text(
                                stringResource(R.string.proceed_to_checkout),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
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
                placeholder = { Text(stringResource(R.string.search_books_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Group books by their effective category
            val categoryOrder = (com.churchbooklibrary.core.datastore.SettingsDataStore.DEFAULT_CATEGORIES + customCategories).distinct().filter { it != "Other" } + "Other"
            val grouped = books.groupBy { bookWithAvail ->
                val cat = bookWithAvail.book.category
                if (cat != null && cat in enabledCategories) cat
                else "Other"
            }
            // Build shelf list: enabled categories first (in order), then Other
            val shelves = categoryOrder.filter { cat ->
                cat == "Other" || cat in enabledCategories
            }.mapNotNull { cat ->
                val shelfBooks = grouped[cat]
                if (!shelfBooks.isNullOrEmpty()) Pair(cat, shelfBooks) else null
            }
            
            val isOnlyOther = shelves.size == 1 && shelves.first().first == "Other"
            val showCategoryShelves = categoriesEnabled && searchQuery.isBlank() && !isOnlyOther

            if (books.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_books_found), color = Slate400)
                }
            } else if (showCategoryShelves) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    shelves.forEach { (shelfName, shelfBooks) ->
                        item(key = "header_$shelfName") {
                            Text(
                                text = getLocalizedCategoryName(shelfName),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        item(key = "row_$shelfName") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(shelfBooks, key = { it.book.id }) { book ->
                                    Box(modifier = Modifier.width(200.dp)) {
                                        BookCard(
                                            book = book,
                                            selectionIndex = if (selectedBooks.containsKey(book.book.id)) selectedBooks.keys.indexOf(book.book.id) + 1 else 0,
                                            availableCopies = book.availableCopies,
                                            isRejected = rejectedBookId == book.book.id,
                                            onToggle = {
                                                if (!selectedBooks.containsKey(book.book.id) && selectedBooks.size >= maxBorrowLimit) {
                                                    rejectedBookId = book.book.id
                                                }
                                                viewModel.toggleBookSelection(book.book.id, book.availableCopies)
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(books.take(60), key = { it.book.id }) { book ->
                        BookCard(
                            book = book,
                            selectionIndex = if (selectedBooks.containsKey(book.book.id)) selectedBooks.keys.indexOf(book.book.id) + 1 else 0,
                            availableCopies = book.availableCopies,
                            isRejected = rejectedBookId == book.book.id,
                            onToggle = {
                                if (!selectedBooks.containsKey(book.book.id) && selectedBooks.size >= maxBorrowLimit) {
                                    rejectedBookId = book.book.id
                                }
                                viewModel.toggleBookSelection(book.book.id, book.availableCopies)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookCard(
    book: com.churchbooklibrary.core.database.BookWithAvailability,
    selectionIndex: Int,
    availableCopies: Int,
    isRejected: Boolean = false,
    onToggle: () -> Unit
) {
    val isSelected = selectionIndex > 0
    val isUnavailable = availableCopies <= 0 && !isSelected
    val borderColor = if (isRejected) Color(0xFFB91C1C) else if (isSelected) com.churchbooklibrary.theme.Blue600 else com.churchbooklibrary.theme.Slate200
    val bgColor = if (isRejected) Color(0xFFFEE2E2) else if (isSelected) com.churchbooklibrary.theme.Blue100 else Color.White

    androidx.compose.material3.Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth().alpha(if (isUnavailable) 0.5f else 1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = bgColor),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                BookCoverImage(
                    title = book.book.title,
                    author = book.book.author,
                    coverUrl = book.book.coverUrl,
                    modifier = Modifier.fillMaxSize()
                )
                if (isRejected) {
                    androidx.compose.material3.Surface(
                        color = Color(0xFFDC2626).copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.limit_reached),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } else if (isUnavailable) {
                    androidx.compose.material3.Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                    Text(
                        text = stringResource(R.string.out_of_stock),
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isSelected) {
                    androidx.compose.material3.Surface(
                        color = com.churchbooklibrary.theme.Blue600.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                    Text(
                        text = selectionIndex.toString(),
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = book.book.title,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = book.book.author,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = com.churchbooklibrary.theme.Slate600
                )
            }
        }
    }
}

@Composable
fun BookCoverImage(
    title: String,
    author: String,
    coverUrl: String?,
    modifier: Modifier = Modifier
) {
    if (coverUrl != null && coverUrl.isNotBlank()) {
        val model = if (coverUrl.startsWith("/")) "file://$coverUrl" else coverUrl
        coil3.compose.AsyncImage(
            model = model,
            contentDescription = stringResource(R.string.cover_for_title, title),
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        val colors = listOf(
            Color(0xFF3B82F6), // Blue
            Color(0xFF8B5CF6), // Purple
            Color(0xFF10B981), // Green
            Color(0xFFF59E0B), // Orange
            Color(0xFFEF4444)  // Red
        )
        val colorIndex = Math.abs(title.hashCode()) % colors.size
        val bgColor = colors[colorIndex]
        
        val initials = author.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")

        androidx.compose.material3.Surface(
            modifier = modifier,
            color = bgColor
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (initials.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = initials,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

