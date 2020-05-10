import java.util.Random;

public class Score {
    private static int currentWeek = 1;
    private static Random rand = new Random();

    public static int getScore(Character character){
        int score = 0;
        score += ((Integer.parseInt(character.getOffense())) * (Math.ceil(rand.nextInt(101)/10.0)));
        score += ((Integer.parseInt(character.getDefense())) * (Math.ceil(rand.nextInt(101)/10.0)));
        score += ((Integer.parseInt(character.getSupport())) * (Math.ceil(rand.nextInt(101)/10.0)));
        return score;
    }

    public static void currentWeekPlus(){
        currentWeek ++;
    }

    public static int getCurrentWeek(){
        return currentWeek;
    }
}
