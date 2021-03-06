/**
 * The Character object.
 */
public class Character {
    /**
     * Name of the Character.
     */
    private String charName;

    /**
     * Offense stat of the character
     */
    private String offense;

    /**
     * Defense stat of the character
     */
    private String defense;

    /**
     * Support stat of the character
     */
    private String support;

    /**
     * Instantiates a new Character object.
     * @param charName the character name
     */
    public Character(String charName) {
        this.charName = charName;
        offense = DataBase.getStats(charName)[1];
        defense = DataBase.getStats(charName)[2];
        support = DataBase.getStats(charName)[3];
    }

    /**
     * Gets character name.
     * @return the character name
     */
    public String getCharName() {
        return charName;
    }

    /**
     * Gets offense.
     * @return the offense
     */
    public String getOffense() {
        return offense;
    }

    /**
     * Gets defense.
     * @return the defense
     */
    public String getDefense() {
        return defense;
    }

    /**
     * Gets support.
     * @return the support
     */
    public String getSupport() {
        return support;
    }

    /**
     * Returns the character name.
     * @return the character name
     */
    @Override
    public String toString() {
        return charName;
    }
}
