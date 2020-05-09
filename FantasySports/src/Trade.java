public class Trade {
    private int sender, receiver;
    private String offer, want;

    public Trade(int sender, int receiver, String offer, String want) {
        this.sender = sender;
        this.receiver = receiver;
        this.offer = offer;
        this.want = want;
    }

    public int getSender() {
        return sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public String getOffer() {
        return offer;
    }

    public String getWant() {
        return want;
    }
}
