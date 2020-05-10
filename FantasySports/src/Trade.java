/**
 * Class to trade players.
 */
public class Trade {
    /**
     * The sender of the trade.
     */
    private int sender;
    /**
     * The receiver of the trade.
     */
    private int receiver;
    /**
     * The character offered in the trade.
     */
    private String offer;
    /**
     * The character wanted from the trade.
     */
    private String want;

    /**
     * Instantiates a new Trade.
     * @param sender the sender
     * @param receiver the receiver
     * @param offer the offer
     * @param want the want
     */
    public Trade(int sender, int receiver, String offer, String want) {
        this.sender = sender;
        this.receiver = receiver;
        this.offer = offer;
        this.want = want;
    }

    /**
     * Gets sender of trade.
     * @return the sender
     */
    public int getSender() {
        return sender;
    }

    /**
     * Gets receiver of trade.
     * @return the receiver
     */
    public int getReceiver() {
        return receiver;
    }

    /**
     * Gets offer from trade.
     * @return the offer
     */
    public String getOffer() {
        return offer;
    }

    /**
     * Gets want from trade.
     * @return the want
     */
    public String getWant() {
        return want;
    }
}
