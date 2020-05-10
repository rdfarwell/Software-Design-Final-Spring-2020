import java.util.Arrays;
import java.util.HashMap;

/**
 * The type Team.
 */
public class Team {
    /**
     * Array of characters on a team.
     */
    private Character[] teamMates = new Character[6];
    /**
     * Hashmap of is character is on team.
     */
    private HashMap<Integer, Boolean> onTeam = new HashMap<>();
    /**
     * Hashmap of character scores.
     */
    private HashMap<Character, Integer> charScores = new HashMap<>();
    /**
     * Total score of team.
     */
    private int totalScore = 0;
    /**
     * Weekly score of team.
     */
    private int weeklyScore = 0;
    /**
     * Temp score variable.
     */
    private int score = 0;

    /**
     * Instantiates a new Team.
     */
    public Team() {
        onTeam.put(0, false);
        onTeam.put(1, false);
        onTeam.put(2, false);
        onTeam.put(3, false);
        onTeam.put(4, false);
        onTeam.put(5, false);
    }

    /**
     * Add team mate.
     * @param name the name of character
     */
    public void addTeamMate(String name) {
        for (int i = 0; i < 6; i++) {
            if (!onTeam.get(i)) {
                onTeam.put(i, true);
                teamMates[i] = new Character(name);
                break;
            }
        }
    }

    /**
     * Checks if team is full.
     * @return true or false
     */
    public boolean fullTeam() {
        return onTeam.get(5);
    }

    /**
     * Checks if team has character.
     * @param name the name of character
     * @return the boolean
     */
    public boolean hasCharacter(String name) {
        for (Character member : teamMates) {
            if (member != null && member.getCharName().toUpperCase().equals(name.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Trades players.
     * @param traded the traded
     * @param received the received
     */
    public void trade(String traded, String received) {
        for (int i = 0; i < 6; i++) {
            if (teamMates[i] != null && teamMates[i].getCharName().toUpperCase().equals(traded.toUpperCase())) {
                teamMates[i] = new Character(received);
                break;
            }
        }
    }

    /**
     * Get character scores.
     * @return list of scores
     */
    public String getCharScore(){
        StringBuilder listOfCharScores = new StringBuilder();
        for (Character character : charScores.keySet()){
            listOfCharScores.append(", ").append(character).append(" ").append(charScores.get(character).toString());
        }
        return listOfCharScores.toString();
    }

    /**
     * Add score to Team.
     */
    public void addScore() {
        for (Character character : teamMates){
            score = Score.getScore(character);
            charScores.put(character,score);
            weeklyScore += score;
            totalScore += score;
        }
    }

    /**
     * Reset weekly score.
     */
    public void resetWeeklyScore() {
        weeklyScore = 0;
    }

    /**
     * Reset total score.
     */
    public void resetTotalScore() {
        totalScore = 0;
    }

    /**
     * Gets total score.
     * @return the total score
     */
    public int getTotalScore() {
        return totalScore;
    }

    /**
     * Gets weekly score.
     * @return the weekly score
     */
    public int getWeeklyScore() {
        return weeklyScore;
    }


    /**
     * Returns who is on Team.
     * @return what characters are on a Team
     */
    @Override
    public String toString() {
        return "Team{" + Arrays.toString(teamMates) + '}';
    }
}
