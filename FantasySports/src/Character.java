public class Character {
    private String charName, role;
    private int offense, defense, support;

    public Character(String charName){
        this.charName = charName;

    }

    public String getCharName(){
        return charName;
    }
    public int getOffense(){
        return offense;
    }
    public int getDefense(){
        return defense;
    }
    public int getSupport(){
        return support;
    }
    public String getRole(){
        return role;
    }
}
