package com.churchbooklibrary.core.repository

import com.churchbooklibrary.core.database.BookEntity
import com.churchbooklibrary.core.database.LoanEntity
import com.churchbooklibrary.core.database.LoanWithBook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BorrowerMatchingAndEligibilityTest {

    // ─── matchesBorrower Tests ──────────────────────────────────────────────

    @Test
    fun matchesBorrower_exactEmailMatch() {
        val matches = LibraryRepository.matchesBorrower(
            inputName = "John Doe",
            inputEmail = "john@example.com",
            inputPhone = "",
            borrowerName = "John Doe",
            borrowerEmail = "john@example.com",
            borrowerPhone = ""
        )
        assertTrue(matches)
    }

    @Test
    fun matchesBorrower_caseInsensitiveAndTrimmedEmailMatch() {
        val matches = LibraryRepository.matchesBorrower(
            inputName = "John D.",
            inputEmail = "  JOHN@Example.COM  ",
            inputPhone = "",
            borrowerName = "John Doe",
            borrowerEmail = "john@example.com",
            borrowerPhone = "0400123456"
        )
        assertTrue(matches)
    }

    @Test
    fun matchesBorrower_phoneMatchWithDifferentFormatting() {
        val matches = LibraryRepository.matchesBorrower(
            inputName = "J. Doe",
            inputEmail = "different@example.com",
            inputPhone = "(04) 0012 3456",
            borrowerName = "John Doe",
            borrowerEmail = "john@example.com",
            borrowerPhone = "0400 123 456"
        )
        assertTrue(matches)
    }

    @Test
    fun matchesBorrower_fallbackNameMatchWhenNoConflictingContact() {
        val matches = LibraryRepository.matchesBorrower(
            inputName = "Jane Doe",
            inputEmail = "",
            inputPhone = "",
            borrowerName = "jane doe",
            borrowerEmail = "",
            borrowerPhone = ""
        )
        assertTrue(matches)
    }

    @Test
    fun matchesBorrower_fallbackNameMatchPreventedWhenEmailsDiffer() {
        // Two distinct borrowers who happen to have the same name
        val matches = LibraryRepository.matchesBorrower(
            inputName = "John Smith",
            inputEmail = "john.smith@gmail.com",
            inputPhone = "",
            borrowerName = "John Smith",
            borrowerEmail = "jsmith99@yahoo.com",
            borrowerPhone = ""
        )
        assertFalse(matches)
    }

    @Test
    fun matchesBorrower_fallbackNameMatchPreventedWhenPhonesDiffer() {
        val matches = LibraryRepository.matchesBorrower(
            inputName = "John Smith",
            inputEmail = "",
            inputPhone = "0411 111 111",
            borrowerName = "John Smith",
            borrowerEmail = "",
            borrowerPhone = "0422 222 222"
        )
        assertFalse(matches)
    }

    @Test
    fun matchesBorrower_phoneMatchWithCountryCodePrefix() {
        // e.g. international format +61 400 123 456 vs local format 0400 123 456
        val matches = LibraryRepository.matchesBorrower(
            inputName = "John",
            inputEmail = "",
            inputPhone = "+61 400 123 456",
            borrowerName = "John",
            borrowerEmail = "",
            borrowerPhone = "0400 123 456"
        )
        assertTrue(matches)
    }

    @Test
    fun matchesBorrower_fallbackNameMatchWithMultipleSpaces() {
        val matches = LibraryRepository.matchesBorrower(
            inputName = "John    Doe",
            inputEmail = "",
            inputPhone = "",
            borrowerName = "John Doe",
            borrowerEmail = "",
            borrowerPhone = ""
        )
        assertTrue(matches)
    }

    @Test
    fun matchesBorrower_differentBorrowers() {
        val matches = LibraryRepository.matchesBorrower(
            inputName = "Alice Wonderland",
            inputEmail = "alice@example.com",
            inputPhone = "0400 000 001",
            borrowerName = "Bob Builder",
            borrowerEmail = "bob@example.com",
            borrowerPhone = "0400 000 002"
        )
        assertFalse(matches)
    }

    // ─── Same First Name / Similar Name Tests (e.g. Mike A vs Mike S) ────────

    @Test
    fun matchesBorrower_mikeA_vs_mikeS_differentNamesAndEmails() {
        val matches = LibraryRepository.matchesBorrower(
            inputName = "Mike S",
            inputEmail = "mike.s@church.org",
            inputPhone = "0400 222 222",
            borrowerName = "Mike A",
            borrowerEmail = "mike.a@church.org",
            borrowerPhone = "0400 111 111"
        )
        assertFalse(matches)
    }

    @Test
    fun matchesBorrower_sameFirstName_differentEmails_doesNotMatch() {
        // Both entered just "Mike" as their name, but have different emails
        val matches = LibraryRepository.matchesBorrower(
            inputName = "Mike",
            inputEmail = "mike.s@church.org",
            inputPhone = "0400 222 222",
            borrowerName = "Mike",
            borrowerEmail = "mike.a@church.org",
            borrowerPhone = "0400 111 111"
        )
        assertFalse(matches)
    }

    @Test
    fun matchesBorrower_sameFirstName_emptyInputEmail_doesNotMatchLoanWithEmail() {
        // Mike S types "Mike" in the name field, before entering his email
        // Should NOT falsely match Mike A's loan records!
        val matches = LibraryRepository.matchesBorrower(
            inputName = "Mike",
            inputEmail = "",
            inputPhone = "",
            borrowerName = "Mike",
            borrowerEmail = "mike.a@church.org",
            borrowerPhone = "0400 111 111"
        )
        assertFalse(matches)
    }

    @Test
    fun matchesBorrower_mikeA_matchesOwnLoan() {
        val matches = LibraryRepository.matchesBorrower(
            inputName = "Mike A",
            inputEmail = "mike.a@church.org",
            inputPhone = "",
            borrowerName = "Mike A",
            borrowerEmail = "mike.a@church.org",
            borrowerPhone = "0400 111 111"
        )
        assertTrue(matches)
    }
}
