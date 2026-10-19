package com.churchbooklibrary.feature.borrow

import androidx.lifecycle.ViewModel
import com.churchbooklibrary.R
import com.churchbooklibrary.core.util.UiText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.churchbooklibrary.core.database.BookEntity
import com.churchbooklibrary.core.database.LoanEntity
import com.churchbooklibrary.core.repository.LibraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class BorrowViewModel(
    private val repository: LibraryRepository,
    private val settingsDataStore: com.churchbooklibrary.core.datastore.SettingsDataStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val books: StateFlow<List<com.churchbooklibrary.core.database.BookWithAvailability>> = _searchQuery
        .flatMapLatest { query -> repository.searchBooksWithAvailability(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesEnabled: StateFlow<Boolean> = settingsDataStore.categoriesEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val enabledCategories: StateFlow<Set<String>> = settingsDataStore.enabledCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.churchbooklibrary.core.datastore.SettingsDataStore.DEFAULT_CATEGORIES.toSet())

    val customCategories: StateFlow<Set<String>> = settingsDataStore.customCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val maxBorrowLimit: StateFlow<Int> = settingsDataStore.maxBorrowLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4)

    val borrowPeriods: StateFlow<List<com.churchbooklibrary.core.datastore.BorrowPeriod>> = settingsDataStore.borrowPeriods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.churchbooklibrary.core.datastore.BorrowPeriod.DEFAULTS)

    private val _selectedBooks = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val selectedBooks: StateFlow<Map<Long, Int>> = _selectedBooks.asStateFlow()

    private val _borrowSuccess = MutableStateFlow<BorrowResult?>(null)
    val borrowSuccess: StateFlow<BorrowResult?> = _borrowSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<UiText?>(null)
    val errorMessage: StateFlow<UiText?> = _errorMessage.asStateFlow()

    fun onSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _borrowerActiveLoans = MutableStateFlow<List<com.churchbooklibrary.core.database.LoanWithBook>>(emptyList())
    val borrowerActiveLoans: StateFlow<List<com.churchbooklibrary.core.database.LoanWithBook>> = _borrowerActiveLoans.asStateFlow()

    fun updateBorrowerContact(name: String, email: String, phone: String) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            val trimmedEmail = email.trim()
            val trimmedPhone = phone.trim()
            val hasValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()
            val hasValidPhone = trimmedPhone.count { it.isDigit() } >= 6

            if (hasValidEmail || hasValidPhone) {
                _borrowerActiveLoans.value = repository.getActiveLoansForBorrower(trimmedName, trimmedEmail, trimmedPhone)
            } else {
                _borrowerActiveLoans.value = emptyList()
            }
        }
    }

    fun toggleBookSelection(bookId: Long, availableCopies: Int) {
        if (availableCopies <= 0) {
            _errorMessage.value = UiText.StringResource(R.string.error_book_unavailable)
            return
        }
        val current = _selectedBooks.value.toMutableMap()
        if (current.containsKey(bookId)) {
            current.remove(bookId)
        } else {
            val currentTotal = current.values.sum()
            if (currentTotal >= maxBorrowLimit.value) {
                _errorMessage.value = UiText.StringResource(R.string.error_max_borrow_limit, maxBorrowLimit.value)
                return
            }
            current[bookId] = 1
        }
        _selectedBooks.value = current
    }

    fun updateQuantity(bookId: Long, quantity: Int, availableCopies: Int) {
        val current = _selectedBooks.value.toMutableMap()
        if (quantity <= 0) {
            current.remove(bookId)
        } else if (quantity > availableCopies) {
            _errorMessage.value = UiText.StringResource(R.string.error_only_copies_available, availableCopies)
            current[bookId] = availableCopies
        } else {
            val otherBooksTotal = current.filterKeys { it != bookId }.values.sum()
            if (otherBooksTotal + quantity > maxBorrowLimit.value) {
                _errorMessage.value = UiText.StringResource(R.string.error_max_borrow_total, maxBorrowLimit.value)
                return
            }
            current[bookId] = quantity
        }
        _selectedBooks.value = current
    }

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun lookupBorrowerContactByName(name: String): Pair<String, String>? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val loan = repository.getLatestLoanForBorrowerName(name.trim())
            if (loan != null && loan.borrowerEmail.isNotBlank()) {
                Pair(loan.borrowerEmail, loan.borrowerPhone)
            } else null
        }
    }

    suspend fun lookupBorrowerContactByEmail(email: String): Pair<String, String>? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val loan = repository.getLatestLoanForBorrowerEmail(email.trim())
            if (loan != null && loan.borrowerName.isNotBlank()) {
                Pair(loan.borrowerName, loan.borrowerPhone)
            } else null
        }
    }

    suspend fun lookupBorrowerContact(namePrefix: String): Pair<String, String>? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val loan = repository.getLatestLoanForBorrowerPrefix(namePrefix)
            if (loan != null && loan.borrowerEmail.isNotBlank()) {
                Pair(loan.borrowerEmail, loan.borrowerPhone)
            } else null
        }
    }

    fun validateBorrowInfo(name: String, email: String): Boolean {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        if (trimmedName.isBlank() || trimmedEmail.isBlank()) {
            _errorMessage.value = UiText.StringResource(R.string.error_name_email_required)
            return false
        }
        if (trimmedName.length < 2) {
            _errorMessage.value = UiText.StringResource(R.string.error_valid_name_required)
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _errorMessage.value = UiText.StringResource(R.string.error_valid_email_required)
            return false
        }
        return true
    }

    suspend fun validateBorrowEligibility(
        name: String,
        email: String,
        phone: String
    ): Boolean {
        if (!validateBorrowInfo(name, email)) {
            return false
        }

        val trimmedPhone = phone.trim()
        if (trimmedPhone.isNotEmpty()) {
            if (!android.util.Patterns.PHONE.matcher(trimmedPhone).matches() || trimmedPhone.count { it.isDigit() } < 5) {
                _errorMessage.value = UiText.StringResource(R.string.error_valid_phone_required)
                return false
            }
        }

        val requestedCount = _selectedBooks.value.values.sum()
        if (requestedCount <= 0) {
            _errorMessage.value = UiText.StringResource(R.string.error_no_books_selected)
            return false
        }

        val requestedBookIds = _selectedBooks.value.keys

        val result = repository.validateBorrowEligibility(
            name = name,
            email = email,
            phone = phone,
            requestedBookIds = requestedBookIds,
            requestedCount = requestedCount
        )

        return when (result) {
            is com.churchbooklibrary.core.repository.BorrowValidationResult.Allowed -> true
            is com.churchbooklibrary.core.repository.BorrowValidationResult.Overdue -> {
                val overdue = result.overdueLoans
                if (overdue.size == 1) {
                    val title = overdue.first().book.title
                    _errorMessage.value = UiText.StringResource(R.string.error_overdue_single, title)
                } else {
                    val titles = overdue.map { it.book.title }.distinct().take(2).joinToString(", ") { "\"$it\"" }
                    _errorMessage.value = UiText.StringResource(R.string.error_overdue_multiple, overdue.size, titles)
                }
                false
            }
            is com.churchbooklibrary.core.repository.BorrowValidationResult.DuplicateBook -> {
                val title = result.duplicateLoan.book.title
                _errorMessage.value = UiText.StringResource(R.string.error_duplicate_book, title)
                false
            }
            is com.churchbooklibrary.core.repository.BorrowValidationResult.LimitExceeded -> {
                if (result.remainingAllowed <= 0) {
                    _errorMessage.value = UiText.StringResource(R.string.error_limit_reached, result.activeCount, result.maxLimit)
                } else {
                    _errorMessage.value = UiText.StringResource(R.string.error_limit_exceeded, result.activeCount, result.maxLimit, result.remainingAllowed)
                }
                false
            }
        }
    }

    fun confirmBorrow(
        name: String,
        email: String,
        phone: String,
        durationValue: String,
        durationLabel: String
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()

        viewModelScope.launch {
            if (!validateBorrowEligibility(trimmedName, trimmedEmail, trimmedPhone)) {
                return@launch
            }

            val now = System.currentTimeMillis()
            val dueDate = calculateDueDate(durationValue, now)
            val selections = _selectedBooks.value.toMap()
            var totalCount = 0

            selections.forEach { (bookId, quantity) ->
                totalCount += quantity
                repository.insertLoan(
                    LoanEntity(
                        borrowerName = trimmedName,
                        borrowerEmail = trimmedEmail,
                        borrowerPhone = trimmedPhone,
                        bookId = bookId,
                        borrowDate = now,
                        dueDate = dueDate,
                        durationLabel = durationLabel,
                        status = "active",
                        quantity = quantity
                    )
                )
            }

            _borrowSuccess.value = BorrowResult(
                bookCount = totalCount,
                dueDate = dueDate
            )
            reset()
        }
    }

    fun clearSuccess() {
        _borrowSuccess.value = null
    }

    fun reset() {
        _selectedBooks.value = emptyMap()
        _searchQuery.value = ""
        _borrowerActiveLoans.value = emptyList()
    }

    private fun calculateDueDate(durationValue: String, fromMs: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = fromMs }
        // Supports any admin-configured period (e.g. "10d", "3w", "6m") as well as the legacy "1w"/"2w"/"1m"/"2m".
        com.churchbooklibrary.core.datastore.BorrowPeriod.parse(durationValue)?.addTo(cal)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    class Factory(
        private val repository: LibraryRepository,
        private val settingsDataStore: com.churchbooklibrary.core.datastore.SettingsDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BorrowViewModel(repository, settingsDataStore) as T
    }
}

data class BorrowResult(
    val bookCount: Int,
    val dueDate: Long
)


