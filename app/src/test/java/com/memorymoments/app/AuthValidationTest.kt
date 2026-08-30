package com.memorymoments.app

import com.memorymoments.app.repository.AuthRepository
import com.memorymoments.app.repository.IdentifierType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AuthValidationTest {

    @Test
    fun testValidGmailAddresses() {
        assertEquals(IdentifierType.GMAIL, AuthRepository.getIdentifierType("example@gmail.com"))
        assertEquals(IdentifierType.GMAIL, AuthRepository.getIdentifierType("user.name+tag@gmail.com"))
        assertEquals(IdentifierType.GMAIL, AuthRepository.getIdentifierType("john_doe123@gmail.com"))
        assertEquals(IdentifierType.GMAIL, AuthRepository.getIdentifierType("TEST@GMAIL.COM"))
        assertEquals(IdentifierType.GMAIL, AuthRepository.getIdentifierType("example@yahoo.com"))
        assertEquals(IdentifierType.GMAIL, AuthRepository.getIdentifierType("example@outlook.com"))
    }

    @Test
    fun testInvalidAndNonGmailEmails() {
        // Invalid email formats
        assertEquals(IdentifierType.INVALID, AuthRepository.getIdentifierType("example"))
        assertEquals(IdentifierType.INVALID, AuthRepository.getIdentifierType("example@"))
        assertEquals(IdentifierType.INVALID, AuthRepository.getIdentifierType("@gmail.com"))
        assertEquals(IdentifierType.INVALID, AuthRepository.getIdentifierType("example@gmail"))
        assertEquals(IdentifierType.INVALID, AuthRepository.getIdentifierType("example@.com"))
    }

    @Test
    fun testPhoneNumberValidation() {
        // Fewer than 7 digits
        assertEquals(IdentifierType.INVALID, AuthRepository.getIdentifierType("12345"))
        assertEquals(IdentifierType.INVALID, AuthRepository.getIdentifierType("123456"))

        // Valid phone numbers (7 to 15 digits, formats with dashes / plus / spaces)
        assertEquals(IdentifierType.PHONE, AuthRepository.getIdentifierType("9876543210"))
        assertEquals(IdentifierType.PHONE, AuthRepository.getIdentifierType("1234567890"))
        assertEquals(IdentifierType.PHONE, AuthRepository.getIdentifierType("+1234567890"))
        assertEquals(IdentifierType.PHONE, AuthRepository.getIdentifierType("123-456-7890"))
        assertEquals(IdentifierType.PHONE, AuthRepository.getIdentifierType("1234567890123"))

        // More than 15 digits
        assertEquals(IdentifierType.INVALID, AuthRepository.getIdentifierType("12345678901234567"))

        // Alphabetic strings
        assertEquals(IdentifierType.INVALID, AuthRepository.getIdentifierType("abcdefghij"))
    }

    @Test
    fun testPasswordHashing() {
        val hash1 = AuthRepository.hashPassword("secret123")
        val hash2 = AuthRepository.hashPassword("secret123")
        val hash3 = AuthRepository.hashPassword("differentPassword")

        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hash3)
        assertEquals(64, hash1.length) // SHA-256 hex string is 64 chars
    }

    @Test
    fun testNormalizeIdentifier() {
        assertEquals("test@gmail.com", AuthRepository.normalizeIdentifier("  TEST@GMAIL.COM  "))
        assertEquals("1234567890", AuthRepository.normalizeIdentifier(" 1234567890 "))
    }
}
