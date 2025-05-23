package com.securevault.service;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a secure random password with specified characteristics
     */
    public String generatePassword(int length, boolean includeLowercase, boolean includeUppercase,
                                   boolean includeDigits, boolean includeSpecial) {
        StringBuilder charPool = new StringBuilder();

        if (includeLowercase) charPool.append(LOWERCASE);
        if (includeUppercase) charPool.append(UPPERCASE);
        if (includeDigits) charPool.append(DIGITS);
        if (includeSpecial) charPool.append(SPECIAL);

        if (charPool.length() == 0) {
            throw new IllegalArgumentException("At least one character type must be included");
        }

        // Ensure we have at least one character from each selected group
        StringBuilder password = new StringBuilder(length);

        if (includeLowercase) {
            password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        }
        if (includeUppercase) {
            password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (includeDigits) {
            password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        if (includeSpecial) {
            password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        }

        // Fill the rest of the password with random characters from the pool
        for (int i = password.length(); i < length; i++) {
            int randomIndex = random.nextInt(charPool.length());
            password.append(charPool.charAt(randomIndex));
        }

        // Shuffle the password to randomize the order
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    /**
     * Calculates password strength based on entropy
     * Returns a value between 0-100 representing strength percentage
     */
    public double calculatePasswordStrength(String password) {
        int length = password.length();
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");

        int charsetSize = 0;
        if (hasLower) charsetSize += 26;
        if (hasUpper) charsetSize += 26;
        if (hasDigit) charsetSize += 10;
        if (hasSpecial) charsetSize += 30;

        // Calculate entropy: log2(charsetSize^length)
        double entropy = length * (Math.log(charsetSize) / Math.log(2));

        // Convert entropy to a percentage (max practical entropy is ~100 bits)
        double strengthPercentage = Math.min(100, entropy);

        return strengthPercentage;
    }

    /**
     * Categorizes password strength
     * Returns: "Weak", "Medium", or "Strong"
     */
    public String getPasswordStrengthCategory(String password) {
        double strength = calculatePasswordStrength(password);

        if (strength < 40) {
            return "Weak";
        } else if (strength < 70) {
            return "Medium";
        } else {
            return "Strong";
        }
    }
}