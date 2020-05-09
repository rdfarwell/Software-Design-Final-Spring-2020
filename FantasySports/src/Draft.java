import java.util.Arrays;

public class Draft {
    public static boolean draftable(String[] drafted, String attempt) {
        for (String checks : drafted) {
            if (checks.toUpperCase().equals(attempt.toUpperCase())) {
                return false;
            }
        }
        return true;
    }

    public static boolean validName(String attempt) {
        for (String names : DataBase.getData("Name")) {
            if (names.toUpperCase().equals(attempt.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
