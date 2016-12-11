// Each method does a different user entry validation
public class Validation {

    // Checks if user entry is an integer
    static boolean entryInt(String entryValue) {
        try {
            Integer.parseInt(entryValue);}
        catch (NumberFormatException nfe) {return false;}
        catch (NullPointerException npe) {return false;}
        return true;
    }

    // Checks if user entry is a double
    static boolean entryDouble(String entryValue) {
        try {
            Double.parseDouble(entryValue);}
        catch (NumberFormatException nfe) {return false;}
        catch (NullPointerException npe) {return false;}
        return true;
    }
}