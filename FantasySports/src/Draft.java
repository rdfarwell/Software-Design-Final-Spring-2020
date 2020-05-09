import java.util.Arrays;

public class Draft {
    private static boolean flip = false;

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

    public static int updateCurrentPlayer(int currentPlayer) {
        if (!flip) {
            if (currentPlayer == 3) {
                flip = true;
                return currentPlayer;
            }
            return (currentPlayer + 1) % 4;
        }
        else {
            if (currentPlayer == 0) {
                flip = false;
                return currentPlayer;
            }
            return (currentPlayer - 1) % 4;
        }
    }
}
