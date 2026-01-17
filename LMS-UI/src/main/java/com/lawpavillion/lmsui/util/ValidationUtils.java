package com.lawpavillion.lmsui.util;

import java.time.LocalDate;

/**
 * Utility class for input validation.
 */
public class ValidationUtils {

    /**
     * Check if a string is null or empty.
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validate ISBN format (basic validation).
     * Accepts ISBN-10 or ISBN-13 formats.
     */
    public static boolean isValidISBN(String isbn) {
        if (isEmpty(isbn)) {
            return false;
        }
        
        // Remove hyphens and spaces
        String cleanIsbn = isbn.replaceAll("[\\s-]", "");
        
        // Check if it's 10 or 13 digits
        return cleanIsbn.matches("\\d{10}") || cleanIsbn.matches("\\d{13}");
    }

    /**
     * Validate that a date is not in the future.
     */
    public static boolean isValidPublishedDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isAfter(LocalDate.now());
    }

    /**
     * Validate book title.
     */
    public static boolean isValidTitle(String title) {
        return !isEmpty(title) && title.length() >= 1 && title.length() <= 200;
    }

    /**
     * Validate author name.
     */
    public static boolean isValidAuthor(String author) {
        return !isEmpty(author) && author.length() >= 2 && author.length() <= 100;
    }
}
