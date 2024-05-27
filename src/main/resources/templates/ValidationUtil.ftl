package com.blumeglobal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.*;


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
                if (validationCheck.startsWith("RANGE(") && validationCheck.endsWith(")")) {
                    return validateRange(value, validationCheck);
                } else if (validationCheck.startsWith("DATERANGE(") && validationCheck.endsWith(")")) {
                    return validateDateRange(value, validationCheck);
                } else if (validationCheck.startsWith("ENUM(") && validationCheck.endsWith(")")) {
                    return validateEnum(value, validationCheck);
                } else {
                    throw new IllegalArgumentException("Invalid validation check: " + validationCheck);
                }

        }
    }

    public static boolean validate(int value, int min, int max){
        return isWithinRange(value,min,max);
    }

    private static boolean validateRange(String value, String validationCheck) {
        Pattern pattern = Pattern.compile("RANGE\\((\\d+),(\\d+)\\)");
        Matcher matcher = pattern.matcher(validationCheck);

        if (matcher.matches()) {
            int min = Integer.parseInt(matcher.group(1));
            int max = Integer.parseInt(matcher.group(2));
            try {
                int intValue = Integer.parseInt(value);
                return isWithinRange(intValue, min, max);
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Invalid range format: " + validationCheck);
        }
    }

    private static boolean validateDateRange(String value, String validationCheck) {
        Pattern pattern = Pattern.compile("DATERANGE\\(([^,]+),([^\\)]+)\\)");
        Matcher matcher = pattern.matcher(validationCheck);

        if (matcher.matches()) {
            String startDateStr = matcher.group(1).trim();
            String endDateStr = matcher.group(2).trim();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            try {
                LocalDate date = LocalDate.parse(value, formatter);
                LocalDate startDate = LocalDate.parse(startDateStr, formatter);
                LocalDate endDate = LocalDate.parse(endDateStr, formatter);
                return isWithinDateRange(date, startDate, endDate);
            } catch (DateTimeParseException e) {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Invalid date range format: " + validationCheck);
        }
    }

    private static boolean validateEnum(String value, String validationCheck) {
        Pattern pattern = Pattern.compile("ENUM\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(validationCheck);

        if (matcher.matches()) {
            String valuesStr = matcher.group(1).trim();
            Set<String> validValues = new HashSet<>(Arrays.asList(valuesStr.split(";")));
            return validValues.contains(value);
        } else {
            throw new IllegalArgumentException("Invalid enum format: " + validationCheck);
        }
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

    private static boolean isWithinDateRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return (date.isEqual(startDate) || date.isAfter(startDate)) && (date.isEqual(endDate) || date.isBefore(endDate));
    }
}
