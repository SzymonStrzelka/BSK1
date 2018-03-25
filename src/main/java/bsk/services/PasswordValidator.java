package bsk.services;

public class PasswordValidator {
    private int minLength = 8;
    private int maxLength = 20;
    private int minDigitOccurrences = 0;
    private int minSpecialCharOccurrences = 0;
    private int minLetterOccurrences = 0;

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMinDigitOccurrences() {
        return minDigitOccurrences;
    }

    public void setMinDigitOccurrences(int minDigitOccurrences) {
        this.minDigitOccurrences = minDigitOccurrences;
    }

    public int getMinSpecialCharOccurrences() {
        return minSpecialCharOccurrences;
    }

    public void setMinSpecialCharOccurrences(int minSpecialCharOccurrences) {
        this.minSpecialCharOccurrences = minSpecialCharOccurrences;
    }

    public int getMinLetterOccurrences() {
        return minLetterOccurrences;
    }

    public void setMinLetterOccurrences(int minLetterOccurrences) {
        this.minLetterOccurrences = minLetterOccurrences;
    }

    public PasswordValidator(int minLength, int maxLength,
                             int minDigitOccurrences, int minSpecialCharOccurrences, int minLetterOccurrences) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.minDigitOccurrences = minDigitOccurrences;
        this.minSpecialCharOccurrences = minSpecialCharOccurrences;
        this.minLetterOccurrences = minLetterOccurrences;
    }

    public PasswordValidator() {
    }

    public void validate(String password) throws InvalidPasswordLengthException, InsufficientCharacterOccurrencesException {
        if (password == null || password.length() < minLength || password.length() > maxLength) {
            throw new InvalidPasswordLengthException(minLength, maxLength);
        }

        int lettersCount = 0;
        for (int i = 0; i < password.length(); i++) {
            if ((password.charAt(i) >= 'a' && password.charAt(i) <= 'z') || password.charAt(i) >= 'A' && password.charAt(i) <= 'Z') {
                lettersCount++;
            }
        }
        if (lettersCount < minLetterOccurrences) {
            throw new InsufficientCharacterOccurrencesException(minLetterOccurrences, "letter");
        }

        int specialCharsCount = 0;
        String specialChars = "/*!@#$%^&*()\"{}_[]|\\?/<>,.";
        for (int i = 0; i < password.length(); i++) {
            String test = password.substring(i, i + 1);
            if (specialChars.contains(password.substring(i, i + 1))) {
                specialCharsCount++;
            }
        }
        if (specialCharsCount < minSpecialCharOccurrences) {
            throw new InsufficientCharacterOccurrencesException(minSpecialCharOccurrences, "special character");
        }

        int digitsCount = 0;
        for (int i = 0; i < password.length(); i++) {
            if (password.charAt(i) >= '0' && password.charAt(i) <= '9') {
                digitsCount++;
            }
        }
        if (digitsCount < minDigitOccurrences) {
            throw new InsufficientCharacterOccurrencesException(minDigitOccurrences, "digit");
        }
    }

    public class InvalidPasswordLengthException extends Exception {
        public InvalidPasswordLengthException(int min, int max) {
            super("Password should contain between " + min + " and " + max + " characters.");
        }
    }

    public class InsufficientCharacterOccurrencesException extends Exception {
        public InsufficientCharacterOccurrencesException(int min, String characterGroup) {
            super("Password should contain at least " + min + " " + characterGroup + "(s).");
        }
    }

}
