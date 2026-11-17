package com.bubt.findit.core.util;

import java.util.regex.Pattern;

/** Small input validation helpers used by the registration form and services. */
public final class Validation {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** BUBT student IDs look like "21-04234", so dashes are allowed. */
    private static final Pattern STUDENT_ID_PATTERN =
            Pattern.compile("^[A-Za-z0-9][A-Za-z0-9-]{3,19}$");

    private Validation() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && !email.isBlank() && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidStudentId(String studentId) {
        return studentId != null && !studentId.isBlank()
                && STUDENT_ID_PATTERN.matcher(studentId.trim()).matches();
    }

    /** At least 8 characters and contains both a letter and a digit. */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char ch : password.toCharArray()) {
            if (Character.isLetter(ch)) {
                hasLetter = true;
            }
            if (Character.isDigit(ch)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
