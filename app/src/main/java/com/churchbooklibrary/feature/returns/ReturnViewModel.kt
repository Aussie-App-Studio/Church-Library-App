package com.churchbooklibrary.feature.returns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.churchbooklibrary.core.database.LoanWithBook
import com.churchbooklibrary.core.repository.LibraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ReturnViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val activeLoans: StateFlow<List<LoanWithBook>> = _searchQuery
        .flatMapLatest { query -> repository.searchActiveLoans(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _returnedLoanId = MutableStateFlow<Long?>(null)
    val returnedLoanId: StateFlow<Long?> = _returnedLoanId.asStateFlow()

    fun onSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun returnBook(loanId: Long) {
        viewModelScope.launch {
            repository.markReturned(loanId)
            _returnedLoanId.value = loanId
        }
    }

    fun returnAllBooksForBorrower(borrowerName: String, loanIds: List<Long>) {
        viewModelScope.launch {
            loanIds.forEach { repository.markReturned(it) }
            _returnedLoanId.value = -1L // Special ID to indicate bulk return
        }
    }

    fun clearReturnedEvent() {
        _returnedLoanId.value = null
    }

    class Factory(private val repository: LibraryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ReturnViewModel(repository) as T
    }
}

