public class Character {
    private String charName;
    private int offense, defense, support;

    public Character(String charName) {
        this.charName = charName;
        offense = Integer.parseInt(DataBase.getStats(charName)[2]);
        defense = Integer.parseInt(DataBase.getStats(charName)[3]);
        support = Integer.parseInt(DataBase.getStats(charName)[4]);
    }

    public String getCharName() {
        return charName;
    }

    public int getOffense() {
        return offense;
    }

    public int getDefense() {
        return defense;
    }

    public int getSupport() {
        return support;
    }
}
