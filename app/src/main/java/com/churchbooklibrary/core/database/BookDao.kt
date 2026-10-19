package com.churchbooklibrary.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Query("SELECT b.*, COALESCE(SUM(l.quantity), 0) AS borrowCount, MAX(l.borrowDate) AS lastBorrowedDate FROM books b LEFT JOIN loans l ON b.id = l.bookId GROUP BY b.id ORDER BY b.title ASC")
    fun getBooksWithStats(): Flow<List<BookWithStats>>

    @Query("SELECT b.*, COALESCE(SUM(l.quantity), 0) AS borrowCount, MAX(l.borrowDate) AS lastBorrowedDate FROM books b LEFT JOIN loans l ON b.id = l.bookId WHERE b.title LIKE '%' || :query || '%' OR b.author LIKE '%' || :query || '%' GROUP BY b.id ORDER BY b.title ASC")
    fun searchBooksWithStats(query: String): Flow<List<BookWithStats>>

    @Query("SELECT b.*, b.totalCopies - (SELECT COALESCE(SUM(quantity), 0) FROM loans WHERE bookId = b.id AND status = 'active') AS availableCopies FROM books b WHERE b.title LIKE '%' || :query || '%' OR b.author LIKE '%' || :query || '%' ORDER BY b.title ASC")
    fun searchBooksWithAvailability(query: String): Flow<List<BookWithAvailability>>
    
    @Query("SELECT b.*, b.totalCopies - (SELECT COALESCE(SUM(quantity), 0) FROM loans WHERE bookId = b.id AND status = 'active') AS availableCopies FROM books b ORDER BY b.title ASC")
    fun getAllBooksWithAvailability(): Flow<List<BookWithAvailability>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<BookEntity>)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Update
    suspend fun updateBooks(books: List<BookEntity>)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int

    @Query("SELECT COUNT(*) FROM books WHERE category = :category")
    suspend fun getBookCountForCategory(category: String): Int

    @Query("UPDATE books SET category = 'Other', updatedAt = :timestamp WHERE category = :category")
    suspend fun reassignCategoryToOther(category: String, timestamp: Long)
}

