
public class Hand {
    private int v;
    private boolean s;
    private boolean p;
    public Hand(int hand, boolean soft, boolean pair) {
        v = hand;
        s = soft;
        p = pair;
    }
    
    public Hand(int card) { //constructor for dealer hand
        if (card == 0) {
            v = 11;
            s = true;
            p = false;
        } else {
            v = card + 1;
            s = false;
            p = false;
        }
    }
    
    public Hand(int card1, int card2) { //constructor for player hands
        s = false;
        p = false;
        if (card1 == card2) {
            p = true;
        }
        if (card1 == 0) {
            v = 11;
            s = true;
        }
        else v = card1 + 1;
        if (card2 == 0) {
            if (card1 == 0) v = 12;
            else {
                v += 11;
                s = true;
            }
        }
    }
    
    public Hand(int hand, boolean soft) {
        this(hand, soft, false);
    }
    
    public Hand(Hand h) {
        v = h.val();
        s = h.soft();
    }
    
    public int val() {
        return v;
    }
    
    public boolean soft() {
        return s;
    }
    
    public boolean pair() {
        return p;
    }
    
    public void inc(int i) {
        v += i;
    }
    
    public void harden() {
        s = false;
        v -= 10;
    }
    
    public void addAce() {
        if (s) {
            ++v;
        } else {
            v += 11;
        }
        s = true;
    }
    
    public Hand split() {
        if (!p) return new Hand(this);
        Hand n;
        if (s) n = new Hand(11, true);
        else n = new Hand(v / 2, false);
        return n;
    }
    
    @Override
    public String toString() {
        return (s ? "soft " : "") + v;
    }
}
