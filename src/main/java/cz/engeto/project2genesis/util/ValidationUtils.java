package cz.engeto.project2genesis.util;

public class ValidationUtils {

    public static void validatePersonID(String personID) {
        if (personID == null) {
            throw new IllegalArgumentException("personID cannot be null");
        }
        if (personID.length() != 12) {
            throw new IllegalArgumentException("personID must be exactly 12 characters long");
        }
    }


}
