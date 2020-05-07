import java.util.HashMap;

public class Team {
    Character[] teamMates = new Character[6];
    HashMap<Integer, Boolean> onTeam = new HashMap<>();

    public Team() {
    }

    public void addTeamMate(Character character) {
        for (int i = 0; i < 6; i++) {
            if (!onTeam.get(i)) {
                onTeam.put(i, true);
                teamMates[i] = character;
            }
        }
    }
}
