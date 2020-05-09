import java.util.Arrays;
import java.util.HashMap;

public class Team {
    Character[] teamMates = new Character[6];
    HashMap<Integer, Boolean> onTeam = new HashMap<>();

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

    //TODO bug testing delete before finish
    @Override
    public String toString() {
        return "Team{" + Arrays.toString(teamMates) + '}';
    }
}
