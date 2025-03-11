// SecureVault Password Manager JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Password visibility toggle
    setupPasswordToggles();

    // Password strength meter
    setupPasswordStrengthMeter();

    // Password generator
    setupPasswordGenerator();

    // Copy password to clipboard
    setupClipboardCopy();

    // Form validation
    setupFormValidation();

    // Alert dismissal
    setupAlertDismissal();
});

/**
 * Toggle password visibility in password fields
 */
function setupPasswordToggles() {
    const toggleButtons = document.querySelectorAll('.password-toggle');

    toggleButtons.forEach(button => {
        button.addEventListener('click', function() {
            const passwordField = document.querySelector(this.dataset.target);
            if (passwordField) {
                if (passwordField.type === 'password') {
                    passwordField.type = 'text';
                    this.textContent = 'Hide';
                } else {
                    passwordField.type = 'password';
                    this.textContent = 'Show';
                }
            }
        });
    });
}

/**
 * Password strength measurement and visual indicator
 */
function setupPasswordStrengthMeter() {
    const passwordInput = document.getElementById('password');
    const strengthMeter = document.querySelector('.password-strength');

    if (passwordInput && strengthMeter) {
        passwordInput.addEventListener('input', function() {
            const strength = calculatePasswordStrength(this.value);
            updateStrengthMeter(strengthMeter, strength);
        });
    }
}

/**
 * Calculate password strength score (0-100)
 */
function calculatePasswordStrength(password) {
    if (!password) return 0;

    let score = 0;

    // Length check
    score += Math.min(password.length * 4, 40);

    // Character type checks
    if (/[a-z]/.test(password)) score += 10; // lowercase
    if (/[A-Z]/.test(password)) score += 10; // uppercase
    if (/\d/.test(password)) score += 10;    // digits
    if (/[^a-zA-Z0-9]/.test(password)) score += 15; // special characters

    // Variety check - unique characters
    const uniqueChars = new Set(password.split('')).size;
    score += Math.min(uniqueChars * 2, 15);

    return Math.min(score, 100);
}

/**
 * Update the visual strength meter based on password score
 */
function updateStrengthMeter(meterElement, score) {
    const meterFill = meterElement.querySelector('.strength-meter-fill');
    const strengthLabel = meterElement.querySelector('span');

    // Update the fill width
    if (meterFill) {
        meterFill.style.width = `${score}%`;
    }

    // Update the class and label
    meterElement.classList.remove('weak', 'medium', 'strong');

    if (score < 40) {
        meterElement.classList.add('weak');
        if (strengthLabel) strengthLabel.textContent = 'Weak';
    } else if (score < 70) {
        meterElement.classList.add('medium');
        if (strengthLabel) strengthLabel.textContent = 'Medium';
    } else {
        meterElement.classList.add('strong');
        if (strengthLabel) strengthLabel.textContent = 'Strong';
    }
}

/**
 * Generate secure random passwords
 */
function setupPasswordGenerator() {
    const generateButton = document.querySelector('.generate-password');

    if (generateButton) {
        generateButton.addEventListener('click', function() {
            const passwordField = document.querySelector(this.dataset.target);
            if (passwordField) {
                const length = parseInt(this.dataset.length || 16);
                const password = generatePassword(length);
                passwordField.value = password;
                passwordField.type = 'text';

                // Trigger strength calculation if applicable
                if (passwordField.id === 'password') {
                    const event = new Event('input');
                    passwordField.dispatchEvent(event);
                }

                // Auto-hide after 3 seconds
                setTimeout(() => {
                    passwordField.type = 'password';
                }, 3000);
            }
        });
    }
}

/**
 * Generate a secure random password with mixed character types
 */
function generatePassword(length = 16) {
    const lowercase = 'abcdefghijklmnopqrstuvwxyz';
    const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const numbers = '0123456789';
    const special = '!@#$%^&*()_+-=[]{}|;:,.<>?';

    const allChars = lowercase + uppercase + numbers + special;

    // Ensure we have at least one of each character type
    let password = getRandomChar(lowercase) +
        getRandomChar(uppercase) +
        getRandomChar(numbers) +
        getRandomChar(special);

    // Fill the rest of the password with random characters
    for (let i = 4; i < length; i++) {
        const randomIndex = Math.floor(Math.random() * allChars.length);
        password += allChars[randomIndex];
    }

    // Shuffle the password characters
    return shuffleString(password);
}

/**
 * Get a random character from a string
 */
function getRandomChar(characterSet) {
    return characterSet[Math.floor(Math.random() * characterSet.length)];
}

/**
 * Shuffle a string
 */
function shuffleString(string) {
    const array = string.split('');
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
    return array.join('');
}

/**
 * Copy password to clipboard functionality
 */
function setupClipboardCopy() {
    const copyButtons = document.querySelectorAll('.copy-password');

    copyButtons.forEach(button => {
        button.addEventListener('click', function() {
            const passwordElement = document.querySelector(this.dataset.target);

            if (passwordElement) {
                // Get password text (might be hidden with bullets)
                let passwordText;

                if (passwordElement.tagName === 'INPUT') {
                    passwordText = passwordElement.value;
                } else {
                    // For a masked password shown as dots, we need to access the data attribute
                    passwordText = passwordElement.dataset.password || passwordElement.textContent;
                }

                // Copy to clipboard
                navigator.clipboard.writeText(passwordText)
                    .then(() => {
                        // Show feedback
                        const originalText = this.textContent;
                        this.textContent = 'Copied!';

                        // Reset button text after 2 seconds
                        setTimeout(() => {
                            this.textContent = originalText;
                        }, 2000);
                    })
                    .catch(err => {
                        console.error('Failed to copy: ', err);
                    });
            }
        });
    });
}

/**
 * Form validation
 */
function setupFormValidation() {
    const forms = document.querySelectorAll('form[data-validate="true"]');

    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            const isValid = validateForm(this);

            if (!isValid) {
                event.preventDefault();
            }
        });
    });
}

/**
 * Validate form fields
 */
function validateForm(form) {
    let isValid = true;
    const inputs = form.querySelectorAll('input[required], select[required], textarea[required]');

    // Clear previous error messages
    const errorMessages = form.querySelectorAll('.error-message');
    errorMessages.forEach(el => el.remove());

    inputs.forEach(input => {
        input.classList.remove('is-invalid');

        if (!input.value.trim()) {
            // Required field is empty
            isValid = false;
            input.classList.add('is-invalid');

            // Add error message
            const errorMessage = document.createElement('div');
            errorMessage.className = 'error-message';
            errorMessage.textContent = 'This field is required';
            input.parentNode.appendChild(errorMessage);
        } else if (input.type === 'email' && !validateEmail(input.value)) {
            // Invalid email
            isValid = false;
            input.classList.add('is-invalid');

            // Add error message
            const errorMessage = document.createElement('div');
            errorMessage.className = 'error-message';
            errorMessage.textContent = 'Please enter a valid email address';
            input.parentNode.appendChild(errorMessage);
        } else if (input.id === 'password' && input.value.length < 8) {
            // Password too short
            isValid = false;
            input.classList.add('is-invalid');

            // Add error message
            const errorMessage = document.createElement('div');
            errorMessage.className = 'error-message';
            errorMessage.textContent = 'Password must be at least 8 characters long';
            input.parentNode.appendChild(errorMessage);
        } else if (input.id === 'confirmPassword') {
            // Password confirmation
            const passwordInput = form.querySelector('#password');
            if (passwordInput && input.value !== passwordInput.value) {
                isValid = false;
                input.classList.add('is-invalid');

                // Add error message
                const errorMessage = document.createElement('div');
                errorMessage.className = 'error-message';
                errorMessage.textContent = 'Passwords do not match';
                input.parentNode.appendChild(errorMessage);
            }
        }
    });

    return isValid;
}

/**
 * Validate email format
 */
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

/**
 * Alert dismissal functionality
 */
function setupAlertDismissal() {
    const alerts = document.querySelectorAll('.alert');

    alerts.forEach(alert => {
        const dismissButton = alert.querySelector('.dismiss');

        if (dismissButton) {
            dismissButton.addEventListener('click', function() {
                alert.remove();
            });
        }

        // Auto-dismiss success alerts after 5 seconds
        if (alert.classList.contains('alert-success')) {
            setTimeout(() => {
                alert.style.opacity = '0';
                setTimeout(() => {
                    alert.remove();
                }, 300);
            }, 5000);
        }
    });
}