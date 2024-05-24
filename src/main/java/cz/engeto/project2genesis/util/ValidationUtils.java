package cz.engeto.project2genesis.util;

public class ValidationUtils {

    public static void validatePersonID(String personID) {
        int requiredPersonIdLength = 12;
        if (personID == null) {
            throw new IllegalArgumentException("personID cannot be null");
        }
        if (personID.length() != requiredPersonIdLength) {
            throw new IllegalArgumentException("personID must be exactly " + requiredPersonIdLength + " characters long");
        }
    }


}
