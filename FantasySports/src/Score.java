import java.util.Random;

/**
 * The Score of each team.
 */
public class Score {
    private static int currentWeek = 1;
    private static Random rand = new Random();

    /**
     * Gets combined score.
     * @param character the character
     * @return the combined score
     */
    public static int getScore(Character character){
        int score = 0;
        score += ((Integer.parseInt(character.getOffense())) * (Math.ceil(rand.nextInt(101)/10.0)));
        score += ((Integer.parseInt(character.getDefense())) * (Math.ceil(rand.nextInt(101)/10.0)));
        score += ((Integer.parseInt(character.getSupport())) * (Math.ceil(rand.nextInt(101)/10.0)));
        return score;
    }

    /**
     * Increments the current week.
     */
    public static void currentWeekPlus(){
        currentWeek ++;
    }

    /**
     * Ges the current week.
     * @return the current week
     */
    public static int getCurrentWeek(){
        return currentWeek;
    }
}
