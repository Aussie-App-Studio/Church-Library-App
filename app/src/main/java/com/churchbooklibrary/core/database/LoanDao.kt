package com.churchbooklibrary.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Transaction
    @Query("SELECT * FROM loans WHERE status = 'active' ORDER BY dueDate ASC")
    fun getActiveLoans(): Flow<List<LoanWithBook>>

    @Transaction
    @Query(
        "SELECT * FROM loans WHERE status = 'active' AND (" +
        "borrowerName LIKE '%' || :query || '%' OR " +
        "borrowerEmail LIKE '%' || :query || '%' OR " +
        "borrowerPhone LIKE '%' || :query || '%' OR " +
        "(LENGTH(:digits) >= 3 AND REPLACE(REPLACE(REPLACE(REPLACE(borrowerPhone, ' ', ''), '-', ''), '(', ''), ')', '') LIKE '%' || :digits || '%') OR " +
        "bookId IN (SELECT id FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%')) " +
        "ORDER BY dueDate ASC"
    )
    fun searchActiveLoans(query: String, digits: String = query.filter { it.isDigit() }): Flow<List<LoanWithBook>>

    @Transaction
    @Query("SELECT * FROM loans ORDER BY borrowDate DESC")
    fun getAllLoans(): Flow<List<LoanWithBook>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM loans")
    fun getTotalBooksEverLoaned(): Flow<Int>

    @Query("SELECT * FROM loans")
    fun getAllLoanEntities(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(loans: List<LoanEntity>)

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Update
    suspend fun updateLoans(loans: List<LoanEntity>)

    @Query("UPDATE loans SET status = 'returned', returnedDate = :returnedDate, updatedAt = :returnedDate WHERE id = :loanId")
    suspend fun markReturned(loanId: Long, returnedDate: Long)

    @Query("DELETE FROM loans")
    suspend fun deleteAllLoans()

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM loans WHERE bookId = :bookId AND status = 'active'")
    suspend fun getActiveLoanCountForBook(bookId: Long): Int

    @Query("DELETE FROM loans WHERE bookId = :bookId")
    suspend fun deleteLoansForBook(bookId: Long)

    @Query("SELECT * FROM loans WHERE LOWER(TRIM(borrowerName)) = LOWER(TRIM(:name)) ORDER BY borrowDate DESC LIMIT 1")
    suspend fun getLatestLoanForBorrowerName(name: String): LoanEntity?

    @Query("SELECT * FROM loans WHERE LOWER(TRIM(borrowerEmail)) = LOWER(TRIM(:email)) ORDER BY borrowDate DESC LIMIT 1")
    suspend fun getLatestLoanForBorrowerEmail(email: String): LoanEntity?

    @Query("SELECT * FROM loans WHERE borrowerName LIKE :namePrefix || '%' ORDER BY borrowDate DESC LIMIT 1")
    suspend fun getLatestLoanForBorrowerPrefix(namePrefix: String): LoanEntity?
}




