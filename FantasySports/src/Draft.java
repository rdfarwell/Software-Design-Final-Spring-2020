import javax.xml.crypto.Data;
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

    public static String auto(String[] drafted) {
        String[] names = DataBase.getData("Name");
        String max = "Bastion";
        String[] maxStats = DataBase.getStats(max.toUpperCase());
        for (String name : names) {
            if (draftable(drafted, name)) {
                String[] stats = DataBase.getStats(name.toUpperCase());
                if (Integer.parseInt(stats[1]) + Integer.parseInt(stats[2]) + Integer.parseInt(stats[3]) > Integer.parseInt(maxStats[1]) + Integer.parseInt(maxStats[2]) + Integer.parseInt(maxStats[3])) {
                    max = name;
                    maxStats = stats;
                }
            }
        }

        return max;
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
