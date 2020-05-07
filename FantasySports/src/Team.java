import java.util.Arrays;
import java.util.HashMap;

public class Team {
    Character[] teamMates = new Character[6];
    HashMap<Integer, Boolean> onTeam = new HashMap<>();

    public Team() {
    }

    public void addTeamMate(String name) {
        for (int i = 0; i < 6; i++) {
            if (!onTeam.get(i)) {
                onTeam.put(i, true);
                teamMates[i] = new Character(name);
            }
        }
    }

    public String getTeam(){
        return Arrays.toString(teamMates);
    }

    public void trade(String traded, String received) {
        for (int i = 0; i < 6; i++) {
            if (teamMates[i].getCharName().equals(traded)) {
                teamMates[i] = new Character(received);
            }
        }
    }
}
