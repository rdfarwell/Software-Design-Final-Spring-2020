public class Character {
    private String charName;
    private String offense, defense, support;

    public Character(String charName) {
        this.charName = charName;
        offense = DataBase.getStats(charName)[1];
        defense = DataBase.getStats(charName)[2];
        support = DataBase.getStats(charName)[3];
    }

    public String getCharName() {
        return charName;
    }

    public String getOffense() {
        return offense;
    }

    public String getDefense() {
        return defense;
    }

    public String getSupport() {
        return support;
    }

    @Override
    public String toString() {
        return charName;
    }
}
