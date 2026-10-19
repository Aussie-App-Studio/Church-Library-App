package com.churchbooklibrary.feature.admin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.churchbooklibrary.core.backup.BackupManager
import com.churchbooklibrary.core.database.BookEntity
import com.churchbooklibrary.core.database.LoanWithBook
import com.churchbooklibrary.core.datastore.PinDataStore
import com.churchbooklibrary.core.datastore.SettingsDataStore
import com.churchbooklibrary.core.repository.IsbnLookupResult
import com.churchbooklibrary.core.repository.LibraryRepository
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModel(
    private val repository: LibraryRepository,
    private val pinDataStore: PinDataStore,
    private val settingsDataStore: SettingsDataStore,
    private val syncManager: com.churchbooklibrary.core.sync.OfflineSyncManager
) : ViewModel() {

    // ─── Active Loans ─────────────────────────────────────────────────────────

    val syncStatus: StateFlow<String> = syncManager.syncStatus

    val syncRole: StateFlow<Int> = settingsDataStore.syncRole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val syncDayOfWeek: StateFlow<Int> = settingsDataStore.syncDayOfWeek
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), java.util.Calendar.SUNDAY)
        
    val syncStartHour: StateFlow<Int> = settingsDataStore.syncStartHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)
        
    val syncEndHour: StateFlow<Int> = settingsDataStore.syncEndHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14)

    fun updateSyncSchedule(context: android.content.Context, day: Int, startHour: Int, endHour: Int) {
        viewModelScope.launch {
            settingsDataStore.setSyncSchedule(day, startHour, endHour)
            // Re-schedule based on current role
            val currentRole = syncRole.value
            com.churchbooklibrary.core.sync.SyncSchedulerManager.cancelAllAlarms(context)
            if (currentRole == 1) {
                com.churchbooklibrary.core.sync.SyncSchedulerManager.scheduleTabletAlarms(context, day, startHour, endHour)
            } else if (currentRole == 2) {
                com.churchbooklibrary.core.sync.SyncSchedulerManager.schedulePhoneAlarms(context, day, startHour, endHour)
            }
        }
    }

    fun setSyncRole(context: android.content.Context, role: Int) {
        viewModelScope.launch {
            settingsDataStore.setSyncRole(role)
            com.churchbooklibrary.core.sync.SyncSchedulerManager.cancelAllAlarms(context)
            val day = syncDayOfWeek.value
            val start = syncStartHour.value
            val end = syncEndHour.value
            if (role == 1) {
                com.churchbooklibrary.core.sync.SyncSchedulerManager.scheduleTabletAlarms(context, day, start, end)
            } else if (role == 2) {
                com.churchbooklibrary.core.sync.SyncSchedulerManager.schedulePhoneAlarms(context, day, start, end)
            }
        }
    }

    fun getSyncManager(context: android.content.Context): com.churchbooklibrary.core.sync.OfflineSyncManager {
        return syncManager
    }
    
    fun startPhoneDiscovery(context: android.content.Context) {
        syncManager.startDiscovery()
    }
    
    fun stopPhoneDiscovery(context: android.content.Context) {
        syncManager.stopDiscovery()
    }
    
    fun toggleTabletBroadcasting(context: android.content.Context, enable: Boolean) {
        val intent = android.content.Intent(context, com.churchbooklibrary.core.sync.TabletSyncService::class.java)
        if (enable) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // If foreground service couldn't start, run in-process advertising directly
                try {
                    syncManager.startAdvertising()
                } catch (_: Exception) {}
            }
        } else {
            try {
                context.stopService(intent)
            } catch (e: Exception) {
                // Ignore stop service errors
            }
            syncManager.stopAdvertising(disconnectEndpoints = true)
        }
    }

    val activeLoans: StateFlow<List<LoanWithBook>> =
        repository.getActiveLoans()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBooksEverLoaned: StateFlow<Int> =
        repository.getTotalBooksEverLoaned()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ─── Book Inventory ───────────────────────────────────────────────────────

    private val _bookSearchQuery = MutableStateFlow("")
    val bookSearchQuery: StateFlow<String> = _bookSearchQuery.asStateFlow()

    val books: StateFlow<List<com.churchbooklibrary.core.database.BookWithStats>> = _bookSearchQuery
        .flatMapLatest { query -> repository.searchBooksWithStats(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBookSearch(query: String) {
        _bookSearchQuery.value = query
    }

    private val _inventoryLoadingMessage = MutableStateFlow<String?>(null)
    val inventoryLoadingMessage: StateFlow<String?> = _inventoryLoadingMessage.asStateFlow()

    private val _isAddingBook = MutableStateFlow(false)
    val isAddingBook: StateFlow<Boolean> = _isAddingBook.asStateFlow()
    private var addBookJob: Job? = null
    private var pendingAddBookEntity: BookEntity? = null

    fun skipCoverAndSaveNow() {
        val entity = pendingAddBookEntity ?: return
        if (isDemoMode.value) {
            _statusMessage.value = "It is not possible to add books in Demo mode. Turn off Demo mode to add your own books."
            return
        }
        addBookJob?.cancel()
        addBookJob = viewModelScope.launch {
            _inventoryLoadingMessage.value = "Saving book..."
            try {
                settingsDataStore.setDemoMode(false)
                repository.insertBook(entity, skipCoverLookup = true)
                _statusMessage.value = "Added: ${entity.title} by ${entity.author}"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = "Failed to add book: ${e.message}"
            } finally {
                _isAddingBook.value = false
                _inventoryLoadingMessage.value = null
                pendingAddBookEntity = null
                addBookJob = null
            }
        }
    }

    private val _duplicateBookPrompt = MutableStateFlow<DuplicateBookPromptData?>(null)
    val duplicateBookPrompt: StateFlow<DuplicateBookPromptData?> = _duplicateBookPrompt.asStateFlow()

    fun dismissDuplicatePrompt() {
        _duplicateBookPrompt.value = null
    }

    fun confirmAddDuplicateCopy() {
        val prompt = _duplicateBookPrompt.value ?: return
        _duplicateBookPrompt.value = null
        if (isDemoMode.value) {
            _statusMessage.value = "It is not possible to add books in Demo mode. Turn off Demo mode to add your own books."
            return
        }
        viewModelScope.launch {
            settingsDataStore.setDemoMode(false)
            if (!sameOwner(prompt.existingBook.owner, prompt.newBook.owner)) {
                // Ownership is tracked per book record, so a copy from a different lender (or the library
                // itself) stays its own entry; otherwise removing one lender's books would remove the other's.
                repository.insertBook(prompt.newBook.copy(totalCopies = prompt.additionalCopies))
                _statusMessage.value = "Added: ${prompt.newBook.title} as a separate entry (different owner)"
                return@launch
            }
            val updated = prompt.existingBook.copy(
                totalCopies = prompt.existingBook.totalCopies + prompt.additionalCopies,
                coverUrl = prompt.existingBook.coverUrl ?: prompt.newBook.coverUrl,
                category = prompt.existingBook.category ?: prompt.newBook.category
            )
            repository.updateBook(updated)
            _statusMessage.value = "Updated: Now ${updated.totalCopies} copies of \"${updated.title}\""
        }
    }

    private fun cleanIsbn(isbn: String?): String? =
        isbn?.replace("-", "")?.replace(" ", "")?.trim()?.takeIf { it.isNotBlank() }

    private fun isSameBook(existing: BookEntity, title: String, author: String, isbn: String?): Boolean {
        val a = cleanIsbn(existing.isbn)
        val b = cleanIsbn(isbn)
        if (a != null && b != null && a == b) return true
        return existing.title.trim().equals(title.trim(), ignoreCase = true) &&
            existing.author.trim().equals(author.trim(), ignoreCase = true)
    }

    private fun sameOwner(a: String?, b: String?): Boolean =
        (a?.trim()?.lowercase() ?: "") == (b?.trim()?.lowercase() ?: "")

    private fun csvFingerprint(rows: List<BackupManager.CsvBookRow>): String {
        val canonical = rows.map {
            listOf(
                it.title.trim().lowercase(), it.author.trim().lowercase(),
                cleanIsbn(it.isbn) ?: "", it.owner?.trim()?.lowercase() ?: "", it.totalCopies.toString()
            ).joinToString("|")
        }.sorted().joinToString("\n")
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private suspend fun findExistingDuplicate(title: String, author: String, isbn: String?): BookEntity? =
        repository.getAllBooks().first().find { isSameBook(it, title, author, isbn) }

    fun cancelAddBook() {
        addBookJob?.cancel()
        addBookJob = null
        pendingAddBookEntity = null
        _isAddingBook.value = false
        _inventoryLoadingMessage.value = null
    }

    fun addBook(
        title: String,
        author: String,
        isbn: String? = null,
        owner: String? = null,
        totalCopies: Int = 1,
        coverUrl: String? = null,
        category: String? = null
    ) {
        if (title.isBlank() || author.isBlank()) return
        if (isDemoMode.value) {
            _statusMessage.value = "It is not possible to add books in Demo mode. Turn off Demo mode to add your own books."
            return
        }
        val finalIsbn = isbn?.replace("-", "")?.replace(" ", "")?.trim()?.takeIf { it.isNotBlank() }
        val finalOwner = owner?.trim()?.takeIf { it.isNotBlank() }
        val finalCoverUrl = coverUrl?.trim()?.takeIf { it.isNotBlank() }
        val finalCategory = category?.trim()?.takeIf { it.isNotBlank() }
            ?: repository.autoCategory(title.trim(), author.trim())
        val entity = BookEntity(
            title = title.trim(),
            author = author.trim(),
            isbn = finalIsbn,
            owner = finalOwner,
            category = finalCategory,
            totalCopies = totalCopies,
            coverUrl = finalCoverUrl
        )

        viewModelScope.launch {
            val existing = findExistingDuplicate(entity.title, entity.author, entity.isbn)
            if (existing != null) {
                _duplicateBookPrompt.value = DuplicateBookPromptData(
                    existingBook = existing,
                    newBook = entity,
                    additionalCopies = totalCopies
                )
                return@launch
            }

            pendingAddBookEntity = entity
            addBookJob?.cancel()
            addBookJob = viewModelScope.launch {
                _isAddingBook.value = true
                _inventoryLoadingMessage.value = "Adding your book..."
                val startTime = System.currentTimeMillis()
                try {
                    settingsDataStore.setDemoMode(false)
                    repository.insertBook(entity)
                    _statusMessage.value = "Added: ${title.trim()} by ${author.trim()}"
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < 600) {
                        delay(600 - elapsed)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _statusMessage.value = "Failed to add book: ${e.message}"
                } finally {
                    _isAddingBook.value = false
                    _inventoryLoadingMessage.value = null
                    pendingAddBookEntity = null
                    addBookJob = null
                }
            }
        }
    }

    fun updateBook(book: BookEntity) {
        if (isDemoMode.value) {
            _statusMessage.value = "It is not possible to modify books in Demo mode. Turn off Demo mode to manage your own books."
            return
        }
        viewModelScope.launch {
            settingsDataStore.setDemoMode(false)
            repository.updateBook(book)
        }
    }

    private val _isLookingUpIsbn = MutableStateFlow(false)
    val isLookingUpIsbn: StateFlow<Boolean> = _isLookingUpIsbn.asStateFlow()
    private var isbnLookupJob: Job? = null

    fun cancelIsbnLookup() {
        isbnLookupJob?.cancel()
        isbnLookupJob = null
        _isLookingUpIsbn.value = false
        _inventoryLoadingMessage.value = null
    }

    fun addBookByIsbn(isbn: String) {
        if (isDemoMode.value) {
            _statusMessage.value = "It is not possible to add books in Demo mode. Turn off Demo mode to add your own books."
            return
        }
        isbnLookupJob?.cancel()
        isbnLookupJob = viewModelScope.launch {
            _isLookingUpIsbn.value = true
            _inventoryLoadingMessage.value = "Looking up your book..."
            val startTime = System.currentTimeMillis()
            try {
                when (val result = repository.lookupBookByIsbn(isbn)) {
                    is IsbnLookupResult.Found -> {
                        val book = result.book
                        val existing = findExistingDuplicate(book.title, book.author, book.isbn)
                        if (existing != null) {
                            _duplicateBookPrompt.value = DuplicateBookPromptData(
                                existingBook = existing,
                                newBook = book,
                                additionalCopies = 1
                            )
                        } else {
                            settingsDataStore.setDemoMode(false)
                            repository.insertBook(book)
                            _statusMessage.value = "Added: ${book.title} by ${book.author}"
                        }
                    }
                    IsbnLookupResult.NotFound ->
                        _statusMessage.value = "No book was found for that ISBN. Try adding it manually."
                    IsbnLookupResult.NetworkError ->
                        _statusMessage.value = "Couldn't reach the book databases. Check the internet connection and try again, or add the book manually."
                    IsbnLookupResult.Invalid ->
                        _statusMessage.value = "That doesn't look like a valid ISBN (it should have 10 or 13 digits)."
                }
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < 600) {
                    delay(600 - elapsed)
                }
            } catch (e: CancellationException) {
                // User cancelled the search
                throw e
            } catch (e: Exception) {
                _statusMessage.value = "Lookup failed: ${e.message}"
            } finally {
                _isLookingUpIsbn.value = false
                _inventoryLoadingMessage.value = null
                isbnLookupJob = null
            }
        }
    }

    fun deleteBook(book: BookEntity, onError: (String) -> Unit) {
        if (isDemoMode.value) {
            onError("Cannot delete books in Demo Mode. Turn off Demo mode to manage your own books.")
            return
        }
        viewModelScope.launch {
            settingsDataStore.setDemoMode(false)
            repository.deleteBook(book).onSuccess {
                // Book deleted successfully
            }.onFailure { e ->
                onError(e.message ?: "Cannot delete book.")
            }
        }
    }

    // ─── Status / Toast ───────────────────────────────────────────────────────

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatus() {
        _statusMessage.value = null
    }

    fun setStatusMessage(message: String) {
        _statusMessage.value = message
    }

    // ─── Loans ────────────────────────────────────────────────────────────────

    fun markReturned(loanId: Long) {
        viewModelScope.launch {
            repository.markReturned(loanId)
            _statusMessage.value = "Book marked as returned."
        }
    }

    // ─── Backup / Restore ─────────────────────────────────────────────────────

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val books = repository.getAllBooks().first()
                val allLoans = repository.getAllLoans().first().map { it.loan }
                val json = BackupManager.exportToJson(books, allLoans)
                BackupManager.writeToUri(context, uri, json)
                _statusMessage.value = "Backup exported: ${books.size} books, ${allLoans.size} loans."
            } catch (e: Exception) {
                _statusMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    fun shareBackup(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val books = repository.getAllBooks().first()
                val allLoans = repository.getAllLoans().first().map { it.loan }
                val json = BackupManager.exportToJson(books, allLoans)
                
                val backupsDir = java.io.File(context.cacheDir, "backups")
                backupsDir.mkdirs()
                val file = java.io.File(backupsDir, "library_backup.json")
                file.writeText(json)
                
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "com.churchbooklibrary.fileprovider",
                    file
                )
                
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                // start activity needs to be launched from main thread or handled properly, 
                // but usually createChooser works even from non-activity contexts if flag new task is set
                val chooser = android.content.Intent.createChooser(intent, "Share Library Backup")
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                
                _statusMessage.value = "Preparing to share backup..."
            } catch (e: Exception) {
                _statusMessage.value = "Failed to share backup: ${e.message}"
            }
        }
    }
    private suspend fun buildCatalog(
        fields: Set<com.churchbooklibrary.core.backup.CatalogField>,
        format: com.churchbooklibrary.core.backup.CatalogFormat
    ): Pair<String, Int> {
        val books = repository.searchBooksWithAvailability("").first()
        return com.churchbooklibrary.core.backup.CatalogExporter.export(books, fields, format) to books.size
    }

    fun exportCatalog(
        context: Context,
        uri: Uri,
        fields: Set<com.churchbooklibrary.core.backup.CatalogField>,
        format: com.churchbooklibrary.core.backup.CatalogFormat
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (text, count) = buildCatalog(fields, format)
                BackupManager.writeToUri(context, uri, text)
                _statusMessage.value = "Catalog exported: $count books (${format.label})."
            } catch (e: Exception) {
                _statusMessage.value = "Catalog export failed: ${e.message}"
            }
        }
    }

    fun shareCatalog(
        context: Context,
        fields: Set<com.churchbooklibrary.core.backup.CatalogField>,
        format: com.churchbooklibrary.core.backup.CatalogFormat
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (text, count) = buildCatalog(fields, format)
                val dir = java.io.File(context.cacheDir, "backups")
                dir.mkdirs()
                val file = java.io.File(dir, "library_catalog.${format.extension}")
                file.writeText(text, Charsets.UTF_8)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context, "com.churchbooklibrary.fileprovider", file
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = format.mimeType
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = android.content.Intent.createChooser(intent, "Share Library Catalog")
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                _statusMessage.value = "Sharing catalog: $count books (${format.label})."
            } catch (e: Exception) {
                _statusMessage.value = "Failed to share catalog: ${e.message}"
            }
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val backup = BackupManager.readFromUri(context, uri)
                // Leave demo mode first: the repository ignores inserts while it is on.
                settingsDataStore.setDemoMode(false)
                val books = backup.books.map { with(BackupManager) { it.toEntity() } }
                val bookIds = books.map { it.id }.toSet()
                // Loans reference books by id, so skip any orphaned loan rather than fail the whole restore.
                val loans = backup.loans
                    .filter { it.bookId in bookIds }
                    .map { with(BackupManager) { it.toEntity() } }
                val skipped = backup.loans.size - loans.size
                repository.insertAllBooks(books)
                repository.insertAllLoans(loans)
                _statusMessage.value = buildString {
                    append("Restored ${books.size} books and ${loans.size} loans.")
                    if (skipped > 0) append(" $skipped loans skipped (book missing).")
                }
            } catch (e: Exception) {
                _statusMessage.value = "Import failed: ${e.message}"
            }
        }
    }

    val coverSyncProgress = repository.coverSyncProgress

    fun startDeepCoverSearch() {
        repository.startDeepCoverSync()
    }

    fun importCsv(csvText: String) {
        if (isDemoMode.value) {
            _statusMessage.value = "It is not possible to add books in Demo mode. Turn off Demo mode to add your own books."
            return
        }
        viewModelScope.launch {
            _inventoryLoadingMessage.value = "Importing books..."
            val startTime = System.currentTimeMillis()
            try {
                val parseResult = BackupManager.parseCsv(csvText)
                val rows = parseResult.rows
                val skippedMalformed = parseResult.skippedMalformedCount
                if (rows.isEmpty()) {
                    _statusMessage.value = if (skippedMalformed > 0) {
                        "No valid books found ($skippedMalformed malformed rows skipped). Use format: Title, Author, [ISBN], [Owner], [Category], [Copies]"
                    } else {
                        "No valid rows found. Use format: Title, Author, [ISBN], [Owner], [Category], [Copies]"
                    }
                    return@launch
                }
                val existingBooks = repository.getAllBooks().first()
                val fingerprint = csvFingerprint(rows)
                val seenBefore = fingerprint in settingsDataStore.importedCsvHashes.first()

                // Importing the identical file twice is a mistake. Only block it while all of its books are
                // still in the library; if they were deleted since, the admin may genuinely want them back.
                if (seenBefore && rows.all { row ->
                        existingBooks.any { isSameBook(it, row.title, row.author, row.isbn) && sameOwner(it.owner, row.owner) }
                    }) {
                    _statusMessage.value = buildString {
                        append("This exact file was already imported and all its books are in your library. Nothing was changed.")
                        if (skippedMalformed > 0) append(" ($skippedMalformed malformed rows were skipped)")
                    }
                    return@launch
                }

                // A row that matches a book already in the library (same owner) adds copies to it.
                // A row that repeats an earlier row in this file adds copies to that row.
                // Anything else becomes a new book.
                val updatedExisting = LinkedHashMap<Long, BookEntity>()
                val booksToInsert = mutableListOf<BookEntity>()
                var extraCopies = 0
                for (row in rows) {
                    val existing = existingBooks.firstOrNull {
                        isSameBook(it, row.title, row.author, row.isbn) && sameOwner(it.owner, row.owner)
                    }
                    if (existing != null) {
                        val current = updatedExisting[existing.id] ?: existing
                        updatedExisting[existing.id] = current.copy(
                            totalCopies = current.totalCopies + row.totalCopies,
                            category = current.category ?: row.category
                        )
                        extraCopies += row.totalCopies
                        continue
                    }
                    val idx = booksToInsert.indexOfFirst {
                        isSameBook(it, row.title, row.author, row.isbn) && sameOwner(it.owner, row.owner)
                    }
                    if (idx >= 0) {
                        booksToInsert[idx] = booksToInsert[idx].copy(totalCopies = booksToInsert[idx].totalCopies + row.totalCopies)
                    } else {
                        booksToInsert.add(
                            BookEntity(
                                title = row.title,
                                author = row.author,
                                isbn = row.isbn,
                                owner = row.owner,
                                category = row.category,
                                totalCopies = row.totalCopies
                            )
                        )
                    }
                }

                settingsDataStore.setDemoMode(false)
                updatedExisting.values.forEach { repository.updateBook(it) }
                if (booksToInsert.isNotEmpty()) repository.insertAllBooks(booksToInsert)
                settingsDataStore.addImportedCsvHash(fingerprint)
                _statusMessage.value = buildString {
                    append("Imported ${booksToInsert.size} new books")
                    if (extraCopies > 0) append(" and added $extraCopies extra copies to books already in your library")
                    if (skippedMalformed > 0) append(" ($skippedMalformed malformed rows skipped)")
                    append(if (booksToInsert.isNotEmpty()) ". Searching covers in background..." else ".")
                }
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < 800) {
                    delay(800 - elapsed)
                }
                // Deep background cover search on application-level scope
                if (booksToInsert.isNotEmpty()) repository.startDeepCoverSync(targetBooks = booksToInsert)
            } catch (e: Exception) {
                _statusMessage.value = "Import failed: ${e.message}"
            } finally {
                _inventoryLoadingMessage.value = null
            }
        }
    }

    fun toggleDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDemoMode(enabled)
            _statusMessage.value = if (enabled) "Demo Mode Enabled" else "Demo Mode Disabled"
        }
    }

    // ─── PIN Management ───────────────────────────────────────────────────────

    fun changePin(
        currentPin: String,
        newPin: String,
        confirmPin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (!pinDataStore.verifyPin(currentPin)) {
                onError("Current PIN is incorrect.")
                return@launch
            }
            if (newPin.length < 4) {
                onError("New PIN must be at least 4 digits.")
                return@launch
            }
            if (newPin != confirmPin) {
                onError("PINs do not match.")
                return@launch
            }
            pinDataStore.savePin(newPin)
            _statusMessage.value = "PIN changed successfully."
            onSuccess()
        }
    }

    val isDefaultPin: StateFlow<Boolean> = pinDataStore.isDefaultPin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // ─── Settings ─────────────────────────────────────────────────────────────

    val isDemoMode: StateFlow<Boolean> = settingsDataStore.isDemoMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val libraryName: StateFlow<String> = settingsDataStore.libraryName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_LIBRARY_NAME)

    val hasCustomLogo: StateFlow<Boolean> = settingsDataStore.hasCustomLogo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val logoLastModified: StateFlow<Long> = settingsDataStore.logoLastModified
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)



    val ribbonColor: StateFlow<Long> = settingsDataStore.ribbonColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_RIBBON_COLOR)

    val maxBorrowLimit: StateFlow<Int> = settingsDataStore.maxBorrowLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4)

    val categoriesEnabled: StateFlow<Boolean> = settingsDataStore.categoriesEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val enabledCategories: StateFlow<Set<String>> = settingsDataStore.enabledCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_CATEGORIES.toSet())

    val customCategories: StateFlow<Set<String>> = settingsDataStore.customCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun addCustomCategory(category: String, onError: (String) -> Unit = {}) {
        val cleaned = category.replace("|", "").trim()
        if (cleaned.isBlank()) {
            val msg = "Category name cannot be empty or contain only delimiter characters."
            _statusMessage.value = msg
            onError(msg)
            return
        }
        val allExisting = (SettingsDataStore.DEFAULT_CATEGORIES + customCategories.value)
        if (allExisting.any { it.equals(cleaned, ignoreCase = true) }) {
            val msg = "Category '$cleaned' already exists."
            _statusMessage.value = msg
            onError(msg)
            return
        }
        viewModelScope.launch {
            settingsDataStore.addCustomCategory(cleaned)
            val currentEnabled = enabledCategories.value
            settingsDataStore.setEnabledCategories(currentEnabled + cleaned)
            _statusMessage.value = "Category '$cleaned' added."
        }
    }

    suspend fun getBookCountForCategory(category: String): Int {
        return repository.getBookCountForCategory(category)
    }

    fun removeCustomCategory(category: String) {
        viewModelScope.launch {
            val count = repository.getBookCountForCategory(category)
            if (count > 0) {
                repository.reassignCategoryToOther(category)
            }
            settingsDataStore.removeCustomCategory(category)
            val currentEnabled = enabledCategories.value
            settingsDataStore.setEnabledCategories(currentEnabled - category)
            if (count > 0) {
                _statusMessage.value = "Category '$category' removed ($count book(s) reassigned to Other)."
            } else {
                _statusMessage.value = "Category '$category' removed."
            }
        }
    }

    fun setCategoriesEnabled(enabled: Boolean) {
        viewModelScope.launch { 
            settingsDataStore.setCategoriesEnabled(enabled) 
            if (enabled) {
                repository.autoCategorizeAllBooks()
            }
        }
    }

    fun setEnabledCategories(categories: Set<String>) {
        viewModelScope.launch { settingsDataStore.setEnabledCategories(categories) }
    }

    fun updateBookCategory(book: BookEntity, category: String) {
        viewModelScope.launch {
            repository.updateBook(book.copy(category = category))
        }
    }

    fun updateLibraryName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            _statusMessage.value = "Library name cannot be blank."
            return
        }
        viewModelScope.launch {
            settingsDataStore.setLibraryName(trimmed)
            _statusMessage.value = "Library name updated."
        }
    }

    fun updateRibbonColor(color: Long) {
        viewModelScope.launch {
            settingsDataStore.setRibbonColor(color)
            _statusMessage.value = "Theme color updated."
        }
    }

    fun updateMaxBorrowLimit(limit: Int) {
        if (limit !in 1..20) {
            _statusMessage.value = "Borrow limit must be between 1 and 20 books."
            return
        }
        viewModelScope.launch {
            settingsDataStore.setMaxBorrowLimit(limit)
            _statusMessage.value = "Max borrow limit updated."
        }
    }

    val borrowPeriods: StateFlow<List<com.churchbooklibrary.core.datastore.BorrowPeriod>> = settingsDataStore.borrowPeriods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.churchbooklibrary.core.datastore.BorrowPeriod.DEFAULTS)

    fun updateBorrowPeriods(periods: List<com.churchbooklibrary.core.datastore.BorrowPeriod>) {
        val valid = periods.size == com.churchbooklibrary.core.datastore.BorrowPeriod.PERIOD_COUNT &&
            periods.all { it.count in com.churchbooklibrary.core.datastore.BorrowPeriod.MIN_COUNT..com.churchbooklibrary.core.datastore.BorrowPeriod.MAX_COUNT } &&
            periods.map { it.value }.distinct().size == periods.size
        if (!valid) {
            _statusMessage.value = "Borrow periods must be 4 different values between 1 and 99."
            return
        }
        viewModelScope.launch {
            settingsDataStore.setBorrowPeriods(periods)
            _statusMessage.value = "Borrow periods updated."
        }
    }

    fun saveCustomLogo(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Validate image bounds and format without loading full bitmap into memory
                val boundsOptions = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOptions)
                } ?: run {
                    _statusMessage.value = "Failed to open selected image."
                    return@launch
                }

                if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                    _statusMessage.value = "The selected file is not a valid image."
                    return@launch
                }

                // 2. Read EXIF orientation (handles photos taken with camera/phone at an angle)
                val orientation = try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val exif = ExifInterface(stream)
                        exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                    } ?: ExifInterface.ORIENTATION_NORMAL
                } catch (e: Exception) {
                    ExifInterface.ORIENTATION_NORMAL
                }

                // 3. Subsample during decode to prevent OutOfMemoryError on large camera images
                val maxTargetDim = 1024
                var sampleSize = 1
                while (boundsOptions.outWidth / (sampleSize * 2) >= maxTargetDim ||
                    boundsOptions.outHeight / (sampleSize * 2) >= maxTargetDim
                ) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                }
                val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }

                if (rawBitmap == null) {
                    _statusMessage.value = "Unable to decode image. Please choose another image file."
                    return@launch
                }

                // 4. Correct image rotation if EXIF orientation is present
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                }

                val orientedBitmap = if (!matrix.isIdentity) {
                    val rotated = Bitmap.createBitmap(
                        rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true
                    )
                    if (rotated != rawBitmap) rawBitmap.recycle()
                    rotated
                } else {
                    rawBitmap
                }

                // 5. Final resize down to max 768px for crisp display without memory bloat
                val maxFinalDim = 768
                val origW = orientedBitmap.width
                val origH = orientedBitmap.height
                val finalBitmap = if (origW > maxFinalDim || origH > maxFinalDim) {
                    val scale = maxFinalDim.toFloat() / maxOf(origW, origH)
                    val targetW = (origW * scale).toInt().coerceAtLeast(1)
                    val targetH = (origH * scale).toInt().coerceAtLeast(1)
                    val scaled = Bitmap.createScaledBitmap(orientedBitmap, targetW, targetH, true)
                    if (scaled != orientedBitmap) orientedBitmap.recycle()
                    scaled
                } else {
                    orientedBitmap
                }

                // 6. Save atomically via a temp file to avoid leaving partial or corrupt files
                val tempFile = File(context.filesDir, "logo_temp.png")
                val logoFile = File(context.filesDir, "logo.png")
                FileOutputStream(tempFile).use { outStream ->
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                }
                finalBitmap.recycle()

                if (logoFile.exists()) {
                    logoFile.delete()
                }
                if (!tempFile.renameTo(logoFile)) {
                    tempFile.copyTo(logoFile, overwrite = true)
                    tempFile.delete()
                }

                settingsDataStore.setHasCustomLogo(true)
                _statusMessage.value = "Custom logo saved successfully."
            } catch (e: Exception) {
                _statusMessage.value = "Error saving logo: ${e.message}"
            }
        }
    }

    fun removeCustomLogo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val logoFile = File(context.filesDir, "logo.png")
                if (logoFile.exists()) {
                    logoFile.delete()
                }
                val tempFile = File(context.filesDir, "logo_temp.png")
                if (tempFile.exists()) {
                    tempFile.delete()
                }
                settingsDataStore.setHasCustomLogo(false)
                _statusMessage.value = "Custom logo removed. Default icon restored."
            } catch (e: Exception) {
                _statusMessage.value = "Failed to remove logo: ${e.message}"
            }
        }
    }







    val hasSeenAboutDeveloper: StateFlow<Boolean> = settingsDataStore.hasSeenAboutDeveloper
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun markAboutDeveloperSeen() {
        viewModelScope.launch { settingsDataStore.setHasSeenAboutDeveloper(true) }
    }

    class Factory(
        private val repository: LibraryRepository,
        private val pinDataStore: PinDataStore,
        private val settingsDataStore: SettingsDataStore,
        private val syncManager: com.churchbooklibrary.core.sync.OfflineSyncManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AdminViewModel(repository, pinDataStore, settingsDataStore, syncManager) as T
    }
}

data class DuplicateBookPromptData(
    val existingBook: BookEntity,
    val newBook: BookEntity,
    val additionalCopies: Int = 1
)









