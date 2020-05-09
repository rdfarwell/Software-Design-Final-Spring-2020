public class Draft {
    public static boolean draftable(String[] drafted, String attempt) {
        for (String checks : drafted) {
            if (checks.toUpperCase().equals(attempt.toUpperCase())) {
                return false;
            }
        }
        return true;
    }
}
