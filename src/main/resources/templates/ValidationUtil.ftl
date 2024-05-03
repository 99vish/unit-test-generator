package com.blumeglobal;

public class ValidationUtil {

    public static boolean validate(String value, String validationCheck){
        switch (validationCheck) {
            case "ALPHANUM":
                return isAlphanumeric(value);
            case "NUM":
                return isNumeric(value);
            case "NOTNULL":
                return validateNotNull(value);
            case "ALPHA":
                return isAlphabetic(value);
            case "SPECIAL":
                return containsSpecialCharacters(value);
            default:
                throw new IllegalArgumentException("Invalid validation check: " + validationCheck);
        }
    }

    public static boolean validate(int value, int min, int max){
        return isWithinRange(value,min,max);
    }

    public static boolean isAlphanumeric(String value){
        return value.matches("[a-zA-Z0-9]+");
    }

    public static boolean isNumeric(String value) {
        return value.matches("\\d+");
    }

    public static boolean validateNotNull(String value) {
        return value != null && !value.isEmpty();
    }

    public static boolean isAlphabetic(String value) {
        return value.matches("[a-zA-Z]+");
    }

    public static boolean isWithinRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static boolean containsSpecialCharacters(String value) {
        return !value.matches("[a-zA-Z0-9]+");
    }
}
