package com.obuspartners.modules.common.util;

import java.security.SecureRandom;

import com.obuspartners.modules.common.domain.enums.PasswordStrength;



/**
 * Utility class for checking the strength of a given password.
 *
 * <p>This class provides a static method {@link #checkStrength(String)} that evaluates 
 * a password based on multiple criteria such as length, case variety, digits, 
 * and special characters. The strength is categorized into 
 * {@link PasswordStrength#LOW}, {@link PasswordStrength#MEDIUM}, or {@link PasswordStrength#HIGH}.</p>
 */
public class PasswordHelperService {
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = LOWER.toUpperCase();
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@$!%*?&";
    private static final String ALL = LOWER + UPPER + DIGITS + SPECIAL;

    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates a strong random password.
     *
     * <p>Password characteristics:</p>
     * <ul>
     *   <li>Length: 12–16 characters (configurable)</li>
     *   <li>Contains at least one lowercase letter</li>
     *   <li>Contains at least one uppercase letter</li>
     *   <li>Contains at least one digit</li>
     *   <li>Contains at least one special character (@, $, !, %, *, ?, &)</li>
     * </ul>
     *
     * @return a randomly generated strong password
     */
    public static String generateStrongPassword() {
        int length = 12 + random.nextInt(5); // 12–16 characters

        StringBuilder password = new StringBuilder(length);

        // Ensure all required character types are included
        password.append(getRandomChar(LOWER));
        password.append(getRandomChar(UPPER));
        password.append(getRandomChar(DIGITS));
        password.append(getRandomChar(SPECIAL));

        // Fill remaining with random mix
        for (int i = 4; i < length; i++) {
            password.append(getRandomChar(ALL));
        }

        // Shuffle to avoid predictable pattern
        return shuffleString(password.toString());
    }

    private static char getRandomChar(String source) {
        return source.charAt(random.nextInt(source.length()));
    }

    private static String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }

    /**
     * Evaluates the strength of the provided password and returns a corresponding
     * {@link PasswordStrength} value.
     *
     * <p>The scoring criteria are as follows:</p>
     * <ul>
     *     <li>+1 if length is at least 8 characters</li>
     *     <li>+1 if length is at least 12 characters</li>
     *     <li>+1 if it contains both lowercase and uppercase letters</li>
     *     <li>+1 if it contains at least one digit</li>
     *     <li>+1 if it contains at least one special character (@, $, !, %, *, ?, &)</li>
     * </ul>
     *
     * <p>Mapping:</p>
     * <ul>
     *     <li>Score 0–2 → {@link PasswordStrength#LOW}</li>
     *     <li>Score 3–4 → {@link PasswordStrength#MEDIUM}</li>
     *     <li>Score 5 → {@link PasswordStrength#HIGH}</li>
     * </ul>
     *
     * @param password the password to check
     * @return the evaluated {@link PasswordStrength} (LOW, MEDIUM, or HIGH)
     */
    public static PasswordStrength checkStrength(String password) {
        int score = 0;

        // 1. Check password length
        if (password.length() >= 8) // reasonable minimum length
            score++;
        if (password.length() >= 12) // stronger if 12+ characters
            score++;

        // 2. Check for mixed case (lowercase + uppercase)
        if (password.matches("(?=.*[a-z])(?=.*[A-Z]).+"))
            score++;

        // 3. Check for digits
        if (password.matches("(?=.*\\d).+"))
            score++;

        // 4. Check for special characters
        if (password.matches("(?=.*[@$!%*?&]).+"))
            score++;

        // 5. Map score to strength category
        if (score <= 2)
            return PasswordStrength.LOW;
        else if (score <= 4)
            return PasswordStrength.MEDIUM;
        else
            return PasswordStrength.HIGH;
    }
}
