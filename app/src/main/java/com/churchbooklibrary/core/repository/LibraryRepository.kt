package com.churchbooklibrary.core.repository

import com.churchbooklibrary.core.database.BookDao
import com.churchbooklibrary.core.database.BookEntity
import com.churchbooklibrary.core.database.LoanDao
import com.churchbooklibrary.core.database.LoanEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.churchbooklibrary.core.database.DatabaseSnapshot
import com.churchbooklibrary.core.database.LoanWithBook
import com.churchbooklibrary.core.datastore.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

data class CoverSyncProgress(
    val isRunning: Boolean = false,
    val totalToScan: Int = 0,
    val completed: Int = 0,
    val foundCount: Int = 0
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LibraryRepository(
    private val realBookDao: BookDao,
    private val realLoanDao: LoanDao,
    private val demoBookDao: BookDao,
    private val demoLoanDao: LoanDao,
    private val settingsDataStore: SettingsDataStore
) {

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null

    private val _coverSyncProgress = MutableStateFlow(CoverSyncProgress())
    val coverSyncProgress: StateFlow<CoverSyncProgress> = _coverSyncProgress.asStateFlow()

    private suspend fun currentBookDao(): BookDao =
        if (settingsDataStore.isDemoMode.first()) demoBookDao else realBookDao

    private suspend fun currentLoanDao(): LoanDao =
        if (settingsDataStore.isDemoMode.first()) demoLoanDao else realLoanDao

    // ─── Books ────────────────────────────────────────────────────────────────

    fun getTotalBooksEverLoaned(): Flow<Int> = settingsDataStore.isDemoMode.flatMapLatest { isDemo ->
        if (isDemo) demoLoanDao.getTotalBooksEverLoaned() else realLoanDao.getTotalBooksEverLoaned()
    }

    fun getAllBooks(): Flow<List<BookEntity>> = settingsDataStore.isDemoMode.flatMapLatest { isDemo ->
        if (isDemo) demoBookDao.getAllBooks() else realBookDao.getAllBooks()
    }

    fun searchBooks(query: String): Flow<List<BookEntity>> = settingsDataStore.isDemoMode.flatMapLatest { isDemo ->
        val dao = if (isDemo) demoBookDao else realBookDao
        if (query.isBlank()) dao.getAllBooks() else dao.searchBooks(query)
    }

    fun searchBooksWithStats(query: String): Flow<List<com.churchbooklibrary.core.database.BookWithStats>> = settingsDataStore.isDemoMode.flatMapLatest { isDemo ->
        val dao = if (isDemo) demoBookDao else realBookDao
        if (query.isBlank()) dao.getBooksWithStats() else dao.searchBooksWithStats(query)
    }

    fun searchBooksWithAvailability(query: String): Flow<List<com.churchbooklibrary.core.database.BookWithAvailability>> = settingsDataStore.isDemoMode.flatMapLatest { isDemo ->
        val dao = if (isDemo) demoBookDao else realBookDao
        if (query.isBlank()) dao.getAllBooksWithAvailability() else dao.searchBooksWithAvailability(query)
    }

    suspend fun insertBook(book: BookEntity, skipCoverLookup: Boolean = false): Long {
        if (settingsDataStore.isDemoMode.first()) return -1L
        val finalCoverUrl = if (skipCoverLookup) book.coverUrl else (book.coverUrl ?: fetchCoverUrlFast(book.title, book.author, book.isbn))
        val finalCategory = book.category ?: autoCategory(book.title, book.author)
        val updatedBook = book.copy(coverUrl = finalCoverUrl, category = finalCategory, updatedAt = System.currentTimeMillis())
        settingsDataStore.touchDatabaseUpdateTimestamp()
        return currentBookDao().insertBook(updatedBook)
    }

    suspend fun insertAllBooks(books: List<BookEntity>) {
        if (settingsDataStore.isDemoMode.first()) return
        settingsDataStore.touchDatabaseUpdateTimestamp()
        val prepared = books.map { book ->
            val cat = book.category ?: autoCategory(book.title, book.author)
            book.copy(category = cat, updatedAt = System.currentTimeMillis())
        }
        currentBookDao().insertAll(prepared)
    }

    /**
     * Launches a deep background search for missing covers across the library.
     * Operates without blocking the UI, systematically querying Google Books,
     * Open Library, ISBN CDNs, and multiple title/author variations.
     */
    fun startDeepCoverSync(targetBooks: List<BookEntity>? = null): Job {
        syncJob?.cancel()
        val job = repoScope.launch {
            if (settingsDataStore.isDemoMode.first()) return@launch
            val dao = currentBookDao()
            val allBooks = dao.getAllBooks().first()
            val missingCovers = if (targetBooks != null) {
                val targetKeys = targetBooks.map { it.title.trim().lowercase() to it.author.trim().lowercase() }.toSet()
                allBooks.filter {
                    it.coverUrl == null && (it.title.trim().lowercase() to it.author.trim().lowercase()) in targetKeys
                }
            } else {
                allBooks.filter { it.coverUrl == null }
            }

            if (missingCovers.isEmpty()) {
                _coverSyncProgress.value = CoverSyncProgress(isRunning = false)
                return@launch
            }

            var completedCount = 0
            var foundCount = 0
            _coverSyncProgress.value = CoverSyncProgress(
                isRunning = true,
                totalToScan = missingCovers.size,
                completed = 0,
                foundCount = 0
            )

            // Concurrency limiter: process 2 books in parallel gently
            val semaphore = Semaphore(2)
            coroutineScope {
                missingCovers.forEach { book ->
                    launch {
                        semaphore.withPermit {
                            try {
                                val coverUrl = fetchCoverUrlDeep(book.title, book.author, book.isbn)
                                if (coverUrl != null) {
                                    dao.updateBook(book.copy(coverUrl = coverUrl, updatedAt = System.currentTimeMillis()))
                                    synchronized(this@LibraryRepository) {
                                        foundCount++
                                    }
                                }
                            } catch (_: Exception) {
                            } finally {
                                synchronized(this@LibraryRepository) {
                                    completedCount++
                                    _coverSyncProgress.value = CoverSyncProgress(
                                        isRunning = completedCount < missingCovers.size,
                                        totalToScan = missingCovers.size,
                                        completed = completedCount,
                                        foundCount = foundCount
                                    )
                                }
                            }
                            delay(200) // Polite pause between requests to prevent API throttling
                        }
                    }
                }
            }

            _coverSyncProgress.value = CoverSyncProgress(
                isRunning = false,
                totalToScan = missingCovers.size,
                completed = completedCount,
                foundCount = foundCount
            )
        }
        syncJob = job
        return job
    }

    suspend fun syncMissingCovers(targetBooks: List<BookEntity>? = null) {
        startDeepCoverSync(targetBooks).join()
    }

    /**
     * Looks up a book by ISBN using a multi-source strategy.
     *
     * Strategy:
     *   1. Google Books (strict `isbn:` query) — most accurate metadata + cover
     *   2. Open Library Books API (by ISBN) — rich metadata + reliable cover CDN
     *   3. Open Library Search API — last resort for obscure ISBNs
     *
     * After getting metadata, we always try to upgrade the cover URL using the
     * Open Library cover CDN (covers.openlibrary.org/b/isbn/...) which is highly
     * reliable and indexed by ISBN directly.
     *
     * NOTE: We intentionally do NOT fall back to a generic keyword/title search for covers,
     * because that causes wrong covers to appear for books that share similar titles.
     */
    /**
     * Looks a book up by ISBN. Dashes/spaces are stripped first, and the result says *why* nothing was
     * found: bad ISBN, genuinely not in any catalogue, or the catalogues couldn't be reached at all.
     */
    suspend fun lookupBookByIsbn(rawIsbn: String): IsbnLookupResult {
        val isbn = normalizeIsbn(rawIsbn) ?: return IsbnLookupResult.Invalid
        var reachedAnySource = false
        val book = lookupBookByIsbnInternal(isbn) { reachedAnySource = true }
        return when {
            book != null -> IsbnLookupResult.Found(book)
            reachedAnySource -> IsbnLookupResult.NotFound
            else -> IsbnLookupResult.NetworkError
        }
    }

    /** Strips dashes/spaces; returns null unless the result is a 10 or 13 character ISBN. */
    fun normalizeIsbn(raw: String): String? {
        val cleaned = raw.filter { it.isDigit() || it == 'X' || it == 'x' }.uppercase()
        val valid = (cleaned.length == 13 && cleaned.all { it.isDigit() }) ||
            (cleaned.length == 10 && cleaned.dropLast(1).all { it.isDigit() })
        return cleaned.takeIf { valid }
    }

    /** GET with explicit timeouts. Throws IOException on connection failure or an HTTP error status. */
    private fun httpGetText(url: String, timeoutMs: Int = 6000): String {
        val conn = URL(url).openConnection() as java.net.HttpURLConnection
        try {
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            if (conn.responseCode !in 200..299) throw java.io.IOException("HTTP ${conn.responseCode}")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private suspend fun lookupBookByIsbnInternal(isbn: String, onReached: () -> Unit): BookEntity? = withContext(Dispatchers.IO) {

        // Returns the best available cover for a confirmed ISBN.
        // Prefers Open Library's ISBN-keyed CDN (most reliable for exact edition covers),
        // then falls back to whatever URL was returned from the metadata source.
        fun bestCover(isbn: String, fallbackUrl: String?): String? {
            val olIsbnCover = "https://covers.openlibrary.org/b/isbn/$isbn-L.jpg"
            if (checkOpenLibraryCover(olIsbnCover)) return olIsbnCover
            return fallbackUrl
        }

        // ── 1. Google Books API (strict `isbn:` query) ─────────────────────────
        // This is the most accurate source — it only matches the exact ISBN.
        try {
            val url = "https://www.googleapis.com/books/v1/volumes?q=isbn:$isbn"
            val json = JSONObject(httpGetText(url).also { onReached() })
            val items = json.optJSONArray("items")
            if (items != null && items.length() > 0) {
                val volumeInfo = items.getJSONObject(0).optJSONObject("volumeInfo")
                if (volumeInfo != null) {
                    val title = volumeInfo.optString("title", "").ifBlank { null }
                    if (title != null) {
                        val authorsArr = volumeInfo.optJSONArray("authors")
                        val author = if (authorsArr != null && authorsArr.length() > 0)
                            authorsArr.getString(0) else "Unknown Author"
                        // Google Books thumbnail URLs can be blocked. Prefer OL cover by ISBN.
                        val googleCover = volumeInfo.optJSONObject("imageLinks")
                            ?.optString("thumbnail")
                            ?.replace("http://", "https://")
                            ?.replace("zoom=1", "zoom=3")
                        
                        val categoriesArr = volumeInfo.optJSONArray("categories")
                        val extraKeywords = if (categoriesArr != null) {
                            (0 until categoriesArr.length()).joinToString(" ") { categoriesArr.optString(it, "") }
                        } else ""

                        return@withContext BookEntity(
                            title = title,
                            author = author,
                            coverUrl = bestCover(isbn, googleCover),
                            isbn = isbn,
                            category = autoCategory(title, author, extraKeywords)
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        // ── 2. Open Library Books API (by ISBN) ────────────────────────────────
        // Returns rich data including authors array and cover object with L/M/S sizes.
        try {
            val url = "https://openlibrary.org/api/books?bibkeys=ISBN:$isbn&format=json&jscmd=data"
            val json = JSONObject(httpGetText(url).also { onReached() })
            val bookData = json.optJSONObject("ISBN:$isbn")
            if (bookData != null) {
                val title = bookData.optString("title", "").ifBlank { null }
                if (title != null) {
                    val authorsArray = bookData.optJSONArray("authors")
                    val author = if (authorsArray != null && authorsArray.length() > 0)
                        authorsArray.getJSONObject(0).optString("name", "Unknown Author")
                    else "Unknown Author"
                    val coverObj = bookData.optJSONObject("cover")
                    val olCover = coverObj?.optString("large") ?: coverObj?.optString("medium")
                    
                    val subjectsArr = bookData.optJSONArray("subjects")
                    val extraKeywords = if (subjectsArr != null) {
                        (0 until subjectsArr.length()).joinToString(" ") { subjectsArr.optJSONObject(it)?.optString("name", "") ?: "" }
                    } else ""

                    return@withContext BookEntity(
                        title = title,
                        author = author,
                        coverUrl = olCover ?: bestCover(isbn, null),
                        isbn = isbn,
                        category = autoCategory(title, author, extraKeywords)
                    )
                }
            }
        } catch (_: Exception) {}

        // ── 3. Open Library Search API (last resort for obscure/regional ISBNs) ─
        try {
            val searchUrl = "https://openlibrary.org/search.json?isbn=$isbn&fields=title,author_name,cover_i"
            val searchJson = JSONObject(httpGetText(searchUrl).also { onReached() })
            val docs = searchJson.optJSONArray("docs")
            if (docs != null && docs.length() > 0) {
                val doc = docs.getJSONObject(0)
                val title = doc.optString("title", "").ifBlank { return@withContext null }
                val authorNames = doc.optJSONArray("author_name")
                val author = if (authorNames != null && authorNames.length() > 0)
                    authorNames.getString(0) else "Unknown Author"
                // Try ISBN cover first; fall back to cover_i ID-based URL
                val coverId = if (doc.has("cover_i")) doc.optInt("cover_i") else null
                val coverIdUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-L.jpg" }
                
                val subjectArr = doc.optJSONArray("subject")
                val extraKeywords = if (subjectArr != null) {
                    (0 until subjectArr.length()).joinToString(" ") { subjectArr.optString(it, "") }
                } else ""

                return@withContext BookEntity(
                    title = title,
                    author = author,
                    coverUrl = bestCover(isbn, coverIdUrl),
                    isbn = isbn,
                    category = autoCategory(title, author, extraKeywords)
                )
            }
        } catch (_: Exception) {}

        null
    }

    suspend fun updateBook(book: BookEntity) {
        if (settingsDataStore.isDemoMode.first()) return
        settingsDataStore.touchDatabaseUpdateTimestamp()
        currentBookDao().updateBook(book.copy(updatedAt = System.currentTimeMillis()))
    }

        suspend fun exportDatabaseToJson(): String {
        val books = currentBookDao().getAllBooks().first()
        val loans = currentLoanDao().getAllLoanEntities().first()
        val timestamp = settingsDataStore.lastDatabaseUpdate.first()
        val snapshot = DatabaseSnapshot(timestamp, books, loans)
        return Json.encodeToString(snapshot)
    }

    suspend fun importDatabaseFromJson(jsonString: String) {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = Json.decodeFromString<DatabaseSnapshot>(jsonString)
                currentBookDao().deleteAllBooks()
                currentLoanDao().deleteAllLoans()
                currentBookDao().insertAll(snapshot.books)
                currentLoanDao().insertAll(snapshot.loans)
                settingsDataStore.touchDatabaseUpdateTimestamp() // Bump it to reflect the import
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun mergeDatabaseFromJson(jsonString: String) {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = Json.decodeFromString<DatabaseSnapshot>(jsonString)
                val bookDao = currentBookDao()
                val loanDao = currentLoanDao()

                // Merge Books
                val localBooks = bookDao.getAllBooks().first().associateBy { it.id }
                val booksToInsert = mutableListOf<BookEntity>()
                val booksToUpdate = mutableListOf<BookEntity>()

                snapshot.books.forEach { remoteBook ->
                    val localBook = localBooks[remoteBook.id]
                    if (localBook == null) {
                        booksToInsert.add(remoteBook)
                    } else if (remoteBook.updatedAt > localBook.updatedAt) {
                        booksToUpdate.add(remoteBook)
                    }
                }
                
                if (booksToInsert.isNotEmpty()) bookDao.insertAll(booksToInsert)
                if (booksToUpdate.isNotEmpty()) bookDao.updateBooks(booksToUpdate)

                // Merge Loans
                val localLoans = loanDao.getAllLoanEntities().first().associateBy { it.id }
                val loansToInsert = mutableListOf<LoanEntity>()
                val loansToUpdate = mutableListOf<LoanEntity>()

                snapshot.loans.forEach { remoteLoan ->
                    val localLoan = localLoans[remoteLoan.id]
                    if (localLoan == null) {
                        loansToInsert.add(remoteLoan)
                    } else if (remoteLoan.updatedAt > localLoan.updatedAt) {
                        loansToUpdate.add(remoteLoan)
                    }
                }
                
                if (loansToInsert.isNotEmpty()) loanDao.insertAll(loansToInsert)
                if (loansToUpdate.isNotEmpty()) loanDao.updateLoans(loansToUpdate)

                settingsDataStore.touchDatabaseUpdateTimestamp()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun autoCategorizeAllBooks() = withContext(Dispatchers.IO) {
        if (settingsDataStore.isDemoMode.first()) return@withContext
        val dao = currentBookDao()
        val allBooks = dao.getAllBooks().first()
        val booksToUpdate = allBooks.filter { it.category == null || it.category == "Other" }
            .map { it.copy(category = autoCategory(it.title, it.author)) }
            .filter { it.category != null && it.category != "Other" } // Only update if we successfully categorized it

        if (booksToUpdate.isNotEmpty()) {
            settingsDataStore.touchDatabaseUpdateTimestamp()
            dao.updateBooks(booksToUpdate)
        }
    }

    suspend fun getBookCountForCategory(category: String): Int = withContext(Dispatchers.IO) {
        currentBookDao().getBookCountForCategory(category)
    }

    suspend fun reassignCategoryToOther(category: String) = withContext(Dispatchers.IO) {
        if (!settingsDataStore.isDemoMode.first()) {
            settingsDataStore.touchDatabaseUpdateTimestamp()
            currentBookDao().reassignCategoryToOther(category, System.currentTimeMillis())
        }
    }

    suspend fun deleteBook(book: BookEntity): Result<Unit> {
        if (settingsDataStore.isDemoMode.first()) return Result.failure(IllegalStateException("Cannot delete in Demo Mode."))
        return try {
            settingsDataStore.touchDatabaseUpdateTimestamp()
            currentLoanDao().deleteLoansForBook(book.id)
            currentBookDao().deleteBook(book)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllBooks() {
        if (settingsDataStore.isDemoMode.first()) return
        currentBookDao().deleteAllBooks()
    }

    // ─── Loans ────────────────────────────────────────────────────────────────

    fun getActiveLoans(): Flow<List<LoanWithBook>> = settingsDataStore.isDemoMode.flatMapLatest { isDemo ->
        if (isDemo) demoLoanDao.getActiveLoans() else realLoanDao.getActiveLoans()
    }

    fun searchActiveLoans(query: String): Flow<List<LoanWithBook>> = settingsDataStore.isDemoMode.flatMapLatest { isDemo ->
        val dao = if (isDemo) demoLoanDao else realLoanDao
        val trimmed = query.trim()
        val digits = trimmed.filter { it.isDigit() }
        if (trimmed.isBlank()) dao.getActiveLoans() else dao.searchActiveLoans(trimmed, digits)
    }

    fun getAllLoans(): Flow<List<LoanWithBook>> = settingsDataStore.isDemoMode.flatMapLatest { isDemo ->
        if (isDemo) demoLoanDao.getAllLoans() else realLoanDao.getAllLoans()
    }

    suspend fun insertLoan(loan: LoanEntity): Long {
        settingsDataStore.touchDatabaseUpdateTimestamp()
        return currentLoanDao().insertLoan(loan.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun insertAllLoans(loans: List<LoanEntity>) {
        if (settingsDataStore.isDemoMode.first()) return
        currentLoanDao().insertAll(loans.map { it.copy(updatedAt = System.currentTimeMillis()) })
    }

    suspend fun markReturned(loanId: Long) =
        currentLoanDao().markReturned(loanId, System.currentTimeMillis())

    
    suspend fun getLatestLoanForBorrowerPrefix(namePrefix: String): com.churchbooklibrary.core.database.LoanEntity? {
        return currentLoanDao().getLatestLoanForBorrowerPrefix(namePrefix)
    }

    suspend fun getLatestLoanForBorrowerName(name: String): com.churchbooklibrary.core.database.LoanEntity? {
        return currentLoanDao().getLatestLoanForBorrowerName(name)
    }

    suspend fun getLatestLoanForBorrowerEmail(email: String): com.churchbooklibrary.core.database.LoanEntity? {
        return currentLoanDao().getLatestLoanForBorrowerEmail(email)
    }

    suspend fun deleteAllLoans() {
        if (settingsDataStore.isDemoMode.first()) return
        currentLoanDao().deleteAllLoans()
    }

    suspend fun getActiveLoansForBorrower(
        name: String,
        email: String,
        phone: String
    ): List<LoanWithBook> = withContext(Dispatchers.IO) {
        val allActive = getActiveLoans().first()
        allActive.filter {
            matchesBorrower(
                inputName = name,
                inputEmail = email,
                inputPhone = phone,
                borrowerName = it.loan.borrowerName,
                borrowerEmail = it.loan.borrowerEmail,
                borrowerPhone = it.loan.borrowerPhone
            )
        }
    }

    suspend fun validateBorrowEligibility(
        name: String,
        email: String,
        phone: String,
        requestedBookIds: Set<Long>,
        requestedCount: Int
    ): BorrowValidationResult = withContext(Dispatchers.IO) {
        val activeLoans = getActiveLoansForBorrower(name, email, phone)
        val now = System.currentTimeMillis()

        // 1. Overdue loan check: Borrower cannot borrow any books if they have overdue loans
        val overdueLoans = activeLoans.filter { it.loan.dueDate < now }
        if (overdueLoans.isNotEmpty()) {
            return@withContext BorrowValidationResult.Overdue(overdueLoans)
        }

        // 2. Duplicate loan check: Borrower cannot borrow a book they currently have out
        val duplicateLoan = activeLoans.firstOrNull { it.loan.bookId in requestedBookIds }
        if (duplicateLoan != null) {
            return@withContext BorrowValidationResult.DuplicateBook(duplicateLoan)
        }

        // 3. Admin limit check: Total active loans + requested count cannot exceed maxBorrowLimit
        val maxLimit = settingsDataStore.maxBorrowLimit.first()
        val activeCount = activeLoans.sumOf { it.loan.quantity.coerceAtLeast(1) }

        if (activeCount + requestedCount > maxLimit) {
            val remainingAllowed = (maxLimit - activeCount).coerceAtLeast(0)
            return@withContext BorrowValidationResult.LimitExceeded(
                activeCount = activeCount,
                requestedCount = requestedCount,
                maxLimit = maxLimit,
                remainingAllowed = remainingAllowed
            )
        }

        BorrowValidationResult.Allowed
    }

    companion object {
        fun matchesBorrower(
            inputName: String,
            inputEmail: String,
            inputPhone: String,
            borrowerName: String,
            borrowerEmail: String,
            borrowerPhone: String
        ): Boolean {
            val normInputEmail = inputEmail.trim().lowercase()
            val normBorrowerEmail = borrowerEmail.trim().lowercase()
            val inputDigits = inputPhone.filter { it.isDigit() }
            val borrowerDigits = borrowerPhone.filter { it.isDigit() }
            val normInputName = inputName.trim().lowercase().replace(Regex("\\s+"), " ")
            val normBorrowerName = borrowerName.trim().lowercase().replace(Regex("\\s+"), " ")

            val emailMatch = normInputEmail.isNotEmpty() && normInputEmail == normBorrowerEmail
            val rawInput = inputDigits.removePrefix("0")
            val rawBorrower = borrowerDigits.removePrefix("0")
            val phoneMatch = (inputDigits.length >= 6 && borrowerDigits.length >= 6 && inputDigits == borrowerDigits) ||
                    (rawInput.length >= 8 && rawBorrower.length >= 8 && (rawInput.endsWith(rawBorrower) || rawBorrower.endsWith(rawInput)))

            if (emailMatch || phoneMatch) {
                return true
            }

            // Fallback to name matching:
            // 1. Both full names must match exactly
            // 2. If the existing loan has an email on file, input MUST provide and match that email
            // 3. If the existing loan has a phone on file (>=6 digits), input MUST provide and match that phone
            if (normInputName.isNotEmpty() && normInputName == normBorrowerName) {
                if (normBorrowerEmail.isNotEmpty()) {
                    return normInputEmail.isNotEmpty() && normInputEmail == normBorrowerEmail
                }
                if (borrowerDigits.length >= 6) {
                    return phoneMatch
                }
                return true
            }

            return false
        }
    }

    // ─── Seed / Reset ─────────────────────────────────────────────────────────

    suspend fun resetAndSeed() {
        // Obsolete in new Demo Mode architecture. Do nothing.
    }

    suspend fun seedDemoDatabase() {
        if (demoBookDao.getBookCount() == 0) {
            val seedBooks = generateSeedBooks()
            demoBookDao.insertAll(seedBooks)
        }
    }

    fun autoCategory(title: String, author: String, extraKeywords: String = ""): String {
    val text = (title + " " + author + " " + extraKeywords).lowercase()
        return when {
            Regex("\\b(bible|kjv|esv|niv|nkjv|nasb|scripture|testament|holy bible)\\b").containsMatchIn(text) -> "Bibles"
            Regex("\\b(commentary|commentaries|exposition|expository|exegesis)\\b").containsMatchIn(text) -> "Commentaries"
            Regex("\\b(biography|biograph|life of|memoir|autobiography|story of)\\b").containsMatchIn(text) -> "Biographies"
            Regex("\\b(devotional|devotion|morning|daily bread|quiet time|prayer|meditat)\\b").containsMatchIn(text) -> "Devotionals"
            Regex("\\b(child|children|kids|toddler|teen|young adult|junior|illustrated|picture book)\\b").containsMatchIn(text) -> "Kids"
            Regex("\\b(theology|theolog|doctrine|systematic|dogmatic|reformed|calvini|sovereignty|eschatol)\\b").containsMatchIn(text) -> "Theology"
            Regex("\\b(study|workbook|curriculum|lesson|small group|sunday school|catechism|concordance)\\b").containsMatchIn(text) -> "Bible Study Material"
            Regex("\\b(apologetic|defense|evidence|reason|case for|atheism|worldview)\\b").containsMatchIn(text) -> "Apologetics"
            Regex("\\b(church history|reformation|puritan|revival|missionary|martyr|spurgeon|edwards|wesley|calvin|luther)\\b").containsMatchIn(text) -> "Church History"
            Regex("\\b(marriage|parenting|family|money|finance|leader|habit|counsel|struggle|addiction|grief|anxiety|depress)\\b").containsMatchIn(text) -> "Practical Christianity"
            Regex("\\b(christian|spiritual|faith|gospel|grace|cross|salvation|sanctif|holy spirit|discipl)\\b").containsMatchIn(text) -> "Christian Living"
            else -> "Other"
        }
    }

    data class CleanedBookMetadata(
        val fullTitle: String,
        val shortTitle: String,
        val fullAuthor: String,
        val authorLastName: String
    )

    fun cleanBookMetadata(title: String, author: String): CleanedBookMetadata {
        var cleanTitle = title.trim()
        cleanTitle = cleanTitle.replace(Regex("\\[.*?\\]"), "").trim()
        cleanTitle = cleanTitle.replace(
            Regex("\\((?:\\d+(?:st|nd|rd|th)?\\s+ed(?:ition)?|revised|anniversary|paperback|hardcover|special|classic|reprint|expanded).*?\\)", RegexOption.IGNORE_CASE),
            ""
        ).trim()

        val shortTitle = cleanTitle.split(":", " - ", "—", " / ").firstOrNull()?.trim() ?: cleanTitle

        var cleanAuthor = author.trim()
        if (cleanAuthor.contains(",") && !cleanAuthor.contains(" and ", ignoreCase = true) && !cleanAuthor.contains("&")) {
            val parts = cleanAuthor.split(",")
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                cleanAuthor = "${parts[1].trim()} ${parts[0].trim()}"
            }
        }
        cleanAuthor = cleanAuthor.replace(Regex("^(?:Dr\\.?|Rev\\.?|Pastor)\\s+", RegexOption.IGNORE_CASE), "").trim()
        cleanAuthor = cleanAuthor.replace(Regex(",?\\s+(?:Ph\\.?D\\.?|D\\.?D\\.?|M\\.?Div\\.?|Jr\\.?|Sr\\.?)$", RegexOption.IGNORE_CASE), "").trim()

        val lastName = cleanAuthor.split(Regex("\\s+")).lastOrNull()?.trim() ?: cleanAuthor

        return CleanedBookMetadata(
            fullTitle = cleanTitle.ifBlank { title.trim() },
            shortTitle = shortTitle.ifBlank { title.trim() },
            fullAuthor = cleanAuthor.ifBlank { author.trim() },
            authorLastName = lastName.ifBlank { author.trim() }
        )
    }

    private fun checkOpenLibraryCover(url: String): Boolean {
        return try {
            val testUrl = if (url.contains("?")) url else "$url?default=false"
            val conn = URL(testUrl).openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.instanceFollowRedirects = true
            conn.connect()
            val contentType = conn.contentType ?: ""
            val ok = conn.responseCode == 200 && (conn.contentLengthLong > 800 || contentType.contains("jpeg"))
            conn.disconnect()
            ok
        } catch (_: Exception) { false }
    }

    private fun queryGoogleBooksForCover(query: String, timeoutMs: Int = 3000): String? {
        return try {
            val url = "https://www.googleapis.com/books/v1/volumes?q=$query"
            val conn = URL(url).openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.instanceFollowRedirects = true
            if (conn.responseCode == 200) {
                val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                val items = json.optJSONArray("items")
                if (items != null) {
                    for (i in 0 until minOf(items.length(), 6)) {
                        val volumeInfo = items.optJSONObject(i)?.optJSONObject("volumeInfo")
                        val imageLinks = volumeInfo?.optJSONObject("imageLinks")
                        val rawThumb = imageLinks?.optString("thumbnail") ?: imageLinks?.optString("smallThumbnail")
                        if (!rawThumb.isNullOrBlank()) {
                            return rawThumb.replace("http://", "https://").replace("zoom=1", "zoom=3")
                        }
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun queryOpenLibraryForCover(query: String, timeoutMs: Int = 3000): String? {
        return try {
            val url = "https://openlibrary.org/search.json?$query&fields=cover_i"
            val conn = URL(url).openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.instanceFollowRedirects = true
            if (conn.responseCode == 200) {
                val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                val docs = json.optJSONArray("docs")
                if (docs != null) {
                    for (i in 0 until minOf(docs.length(), 6)) {
                        val doc = docs.optJSONObject(i)
                        if (doc != null && doc.has("cover_i")) {
                            val coverI = doc.optInt("cover_i")
                            if (coverI > 0) {
                                return "https://covers.openlibrary.org/b/id/$coverI-L.jpg"
                            }
                        }
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Fast-path cover lookup used for single manual additions.
     * Completes within 2-3 seconds to prevent UI blocking.
     */
    suspend fun fetchCoverUrlFast(title: String, author: String, isbn: String? = null): String? = withContext(Dispatchers.IO) {
        if (!isbn.isNullOrBlank()) {
            val cleanIsbn = isbn.replace("-", "").trim()
            val olCover = "https://covers.openlibrary.org/b/isbn/$cleanIsbn-L.jpg"
            if (checkOpenLibraryCover(olCover)) return@withContext olCover
            val gbIsbn = queryGoogleBooksForCover("isbn:$cleanIsbn", timeoutMs = 2500)
            if (gbIsbn != null) return@withContext gbIsbn
        }

        val meta = cleanBookMetadata(title, author)
        val authorParam = if (meta.fullAuthor.isNotBlank() && meta.fullAuthor != "Unknown Author") {
            "+inauthor:${URLEncoder.encode(meta.fullAuthor, "UTF-8")}"
        } else ""

        val gb = queryGoogleBooksForCover("intitle:${URLEncoder.encode(meta.shortTitle, "UTF-8")}$authorParam", timeoutMs = 2500)
        if (gb != null) return@withContext gb

        null
    }

    /**
     * Deep background cover search. Takes all the time necessary without blocking the user,
     * thoroughly querying ISBN CDNs, Google Books with full/short titles and authors,
     * Open Library search by title and author, and broad keyword fallbacks.
     */
    suspend fun fetchCoverUrlDeep(title: String, author: String, isbn: String? = null): String? = withContext(Dispatchers.IO) {
        val meta = cleanBookMetadata(title, author)

        // 1. Direct ISBN lookups (if ISBN present)
        if (!isbn.isNullOrBlank()) {
            val cleanIsbn = isbn.replace("-", "").trim()
            val olCover = "https://covers.openlibrary.org/b/isbn/$cleanIsbn-L.jpg"
            if (checkOpenLibraryCover(olCover)) return@withContext olCover

            val gbIsbn = queryGoogleBooksForCover("isbn:$cleanIsbn", timeoutMs = 5000)
            if (gbIsbn != null) return@withContext gbIsbn

            try {
                val olApiUrl = "https://openlibrary.org/api/books?bibkeys=ISBN:$cleanIsbn&format=json&jscmd=data"
                val conn = URL(olApiUrl).openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val bookData = JSONObject(jsonStr).optJSONObject("ISBN:$cleanIsbn")
                    val coverObj = bookData?.optJSONObject("cover")
                    val found = coverObj?.optString("large") ?: coverObj?.optString("medium")
                    if (!found.isNullOrBlank()) return@withContext found
                }
            } catch (_: Exception) {}
        }

        // 2. Google Books Tier 1: Full Title + Clean Author
        val authorParam = if (meta.fullAuthor.isNotBlank() && meta.fullAuthor != "Unknown Author") {
            "+inauthor:${URLEncoder.encode(meta.fullAuthor, "UTF-8")}"
        } else ""

        val gbFull = queryGoogleBooksForCover("intitle:${URLEncoder.encode(meta.fullTitle, "UTF-8")}$authorParam", timeoutMs = 5000)
        if (gbFull != null) return@withContext gbFull

        // 3. Google Books Tier 2: Short Title (without subtitle) + Clean Author
        if (meta.shortTitle != meta.fullTitle) {
            val gbShort = queryGoogleBooksForCover("intitle:${URLEncoder.encode(meta.shortTitle, "UTF-8")}$authorParam", timeoutMs = 5000)
            if (gbShort != null) return@withContext gbShort
        }

        // 4. Google Books Tier 3: Short Title + Author Last Name
        if (meta.authorLastName.isNotBlank() && meta.authorLastName != meta.fullAuthor && meta.authorLastName != "Unknown Author") {
            val gbLastName = queryGoogleBooksForCover(
                "intitle:${URLEncoder.encode(meta.shortTitle, "UTF-8")}+inauthor:${URLEncoder.encode(meta.authorLastName, "UTF-8")}",
                timeoutMs = 5000
            )
            if (gbLastName != null) return@withContext gbLastName
        }

        // 5. Open Library Search: Short Title + Clean Author
        val olAuthor = if (meta.fullAuthor.isNotBlank() && meta.fullAuthor != "Unknown Author") {
            "&author=${URLEncoder.encode(meta.fullAuthor, "UTF-8")}"
        } else ""
        val olResult = queryOpenLibraryForCover("title=${URLEncoder.encode(meta.shortTitle, "UTF-8")}$olAuthor", timeoutMs = 5000)
        if (olResult != null) return@withContext olResult

        // 6. Open Library Search: Short Title + Author Last Name
        if (meta.authorLastName.isNotBlank() && meta.authorLastName != meta.fullAuthor) {
            val olLastNameResult = queryOpenLibraryForCover(
                "title=${URLEncoder.encode(meta.shortTitle, "UTF-8")}&author=${URLEncoder.encode(meta.authorLastName, "UTF-8")}",
                timeoutMs = 5000
            )
            if (olLastNameResult != null) return@withContext olLastNameResult
        }

        // 7. Google Books Tier 4: Broad general query (title + author without strict intitle)
        val broadQuery = URLEncoder.encode("${meta.shortTitle} ${meta.authorLastName}".trim(), "UTF-8")
        val gbBroad = queryGoogleBooksForCover(broadQuery, timeoutMs = 5000)
        if (gbBroad != null) return@withContext gbBroad

        null
    }

    suspend fun fetchCoverUrl(title: String, author: String, isbn: String? = null): String? {
        return fetchCoverUrlFast(title, author, isbn) ?: fetchCoverUrlDeep(title, author, isbn)
    }

    private suspend fun generateSeedBooks(): List<BookEntity> {
        return listOf(
            BookEntity(title = "Pilgrim’s Progress", author = "John Bunyan", coverUrl = "https://covers.openlibrary.org/b/id/8229224-L.jpg"),
            BookEntity(title = "What Is the Gospel?", author = "Greg Gilbert", coverUrl = "https://covers.openlibrary.org/b/id/6308614-L.jpg"),
            BookEntity(title = "Screwtape Letters", author = "C.S. Lewis", coverUrl = "https://covers.openlibrary.org/b/id/9779-L.jpg"),
            BookEntity(title = "Gentle and Lowly", author = "Dane Ortlund", coverUrl = "https://covers.openlibrary.org/b/id/12391363-L.jpg"),
            BookEntity(title = "Respectable Sins", author = "Jerry Bridges", coverUrl = "https://covers.openlibrary.org/b/id/2007768-L.jpg"),
            BookEntity(title = "Desiring God", author = "John Piper", coverUrl = "https://covers.openlibrary.org/b/id/876658-L.jpg"),
            BookEntity(title = "Mere Christianity", author = "C.S. Lewis", coverUrl = "https://covers.openlibrary.org/b/id/6538891-L.jpg"),
            BookEntity(title = "The Lion, the Witch, and the Wardrobe", author = "C.S. Lewis", coverUrl = "https://covers.openlibrary.org/b/id/8441376-L.jpg"),
            BookEntity(title = "Taking God At His Word", author = "Kevin DeYoung", coverUrl = "https://covers.openlibrary.org/b/id/7278254-L.jpg"),
            BookEntity(title = "Knowing God", author = "J.I. Packer", coverUrl = "https://covers.openlibrary.org/b/id/2402846-L.jpg"),
            BookEntity(title = "You Can Change: God’s Transforming Power", author = "Tim Chester", coverUrl = null),
            BookEntity(title = "The Holiness of God", author = "R.C. Sproul", coverUrl = "https://covers.openlibrary.org/b/id/10162453-L.jpg"),
            BookEntity(title = "Christian Beliefs: Twenty Basics Every Christian Should Know", author = "Wayne Grudem", coverUrl = null),
            BookEntity(title = "Spiritual Disciplines for the Christian Life", author = "Donald S. Whitney", coverUrl = "https://covers.openlibrary.org/b/id/831464-L.jpg"),
            BookEntity(title = "A Praying Life", author = "Paul E. Miller", coverUrl = "https://covers.openlibrary.org/b/id/7432529-L.jpg"),
            BookEntity(title = "Trusting God", author = "Jerry Bridges", coverUrl = "https://covers.openlibrary.org/b/id/688502-L.jpg"),
            BookEntity(title = "Church Membership", author = "Jonathan Cruse", coverUrl = null),
            BookEntity(title = "Nine Marks of a Healthy Church", author = "Mark Dever", coverUrl = "https://covers.openlibrary.org/b/id/841892-L.jpg"),
            BookEntity(title = "Reformed Worship", author = "Jonty Rhodes", coverUrl = null),
            BookEntity(title = "Reformed Theology", author = "Jonathan Master", coverUrl = null),
            BookEntity(title = "The Reason for God", author = "Timothy Keller", coverUrl = "https://covers.openlibrary.org/b/id/6959410-L.jpg"),
            BookEntity(title = "Evangelism and the Sovereignty of God", author = "J.I. Packer", coverUrl = null),
            BookEntity(title = "Persistent Prayer", author = "Guy Richard", coverUrl = null),
            BookEntity(title = "Holiness", author = "J.C. Ryle", coverUrl = "https://covers.openlibrary.org/b/id/649170-L.jpg"),
            BookEntity(title = "The Valley of Vision", author = "Arthur Bennett", coverUrl = "https://covers.openlibrary.org/b/id/648308-L.jpg")
        )
    }
}

sealed class BorrowValidationResult {
    object Allowed : BorrowValidationResult()
    data class Overdue(val overdueLoans: List<LoanWithBook>) : BorrowValidationResult()
    data class DuplicateBook(val duplicateLoan: LoanWithBook) : BorrowValidationResult()
    data class LimitExceeded(
        val activeCount: Int,
        val requestedCount: Int,
        val maxLimit: Int,
        val remainingAllowed: Int
    ) : BorrowValidationResult()
}







sealed class IsbnLookupResult {
    data class Found(val book: BookEntity) : IsbnLookupResult()
    object NotFound : IsbnLookupResult()
    object NetworkError : IsbnLookupResult()
    object Invalid : IsbnLookupResult()
}

