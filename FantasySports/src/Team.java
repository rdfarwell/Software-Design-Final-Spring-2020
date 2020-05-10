import java.util.Arrays;
import java.util.HashMap;

public class Team {
    private Character[] teamMates = new Character[6];
    private HashMap<Integer, Boolean> onTeam = new HashMap<>();
    private HashMap<Character, Integer> charScores = new HashMap<>();
    private int totalScore = 0;
    private int weeklyScore = 0;
    private int score = 0;

    public Team() {
        onTeam.put(0, false);
        onTeam.put(1, false);
        onTeam.put(2, false);
        onTeam.put(3, false);
        onTeam.put(4, false);
        onTeam.put(5, false);
    }

    public void addTeamMate(String name) {
        for (int i = 0; i < 6; i++) {
            if (!onTeam.get(i)) {
                onTeam.put(i, true);
                teamMates[i] = new Character(name);
                break;
            }
        }
    }

    public String getTeam() {
        return Arrays.toString(teamMates);
    }

    public boolean fullTeam() {
        return onTeam.get(5);
    }

    public boolean hasCharacter(String name) {
        for (Character member : teamMates) {
            if (member != null && member.getCharName().toUpperCase().equals(name.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public void trade(String traded, String received) {
        for (int i = 0; i < 6; i++) {
            if (teamMates[i] != null && teamMates[i].getCharName().toUpperCase().equals(traded.toUpperCase())) {
                teamMates[i] = new Character(received);
                break;
            }
        }
    }

    public String getCharScore(){
        StringBuilder listOfCharScores = new StringBuilder();
        for (Character character : charScores.keySet()){
            listOfCharScores.append(", ").append(character).append(" ").append(charScores.get(character).toString());
        }
        return listOfCharScores.toString();
    }

    public void addScore() {
        for (Character character : teamMates){
            score += Score.getScore(character);
            charScores.put(character,score);
        }
        weeklyScore += score;
        totalScore += score;
    }

    public void resetWeeklyScore() {
        weeklyScore = 0;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getWeeklyScore() {
        return weeklyScore;
    }

    @Override
    public String toString() {
        return "Team{" + Arrays.toString(teamMates) + '}';
    }
}
