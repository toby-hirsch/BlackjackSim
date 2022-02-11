import java.util.Arrays;
public class Shoe {
    private int[] cards;
    private int numCards;
    
    public Shoe() { //Use only if using random() after
        cards = new int[10];
        numCards = 0;
    }
    
    public Shoe(int size) {
        cards = new int[10];
        for (int i = 0; i < 10; i++) {
            cards[i] = size * 4;
        }
        cards[9] = size * 16;
        numCards = size * 52;
    }
    
    public Shoe(Shoe s) {
        cards = s.getCards().clone();
        numCards = s.getNumCards();
    }
    
    public int dealCard() {
        if (numCards <= 0) {
            return -1;
        }
        int idx = (int) (Math.random() * numCards);
        for (int i = 0; i < 10; i++) {
            idx -= cards[i];
            if (idx < 0) {
                --numCards;
                --cards[i];
                return i;
            }
        }
        return -1;
    }

    protected int[] getCards() {
        return cards;
    }
    
    public int getCount(int i) {
        return cards[i];
    }

    protected int getNumCards() {
        return numCards;
    }
    
    public double getProb(int card) {
        return 1.0 * cards[card] / numCards;
    }
    
    public void random(int shoeSize) {
        numCards = 0;
        int rand;
        for (int i = 0; i < 9; i++) {
            rand = (int) (Math.random() * 4 * shoeSize);
            numCards += rand;
            cards[i] = rand;
        }
        rand = (int) (Math.random() * 16 * shoeSize);
        numCards += rand;
        cards[9] = rand;
        if (numCards < shoeSize * 17) random(shoeSize);
    }
    
    public void removeCard(int card) {
        if (cards[card] <= 0) {
            throw new IllegalStateException();
        }
        --numCards;
        --cards[card];
    }
    
    @Override
    public String toString() {
        return "(Size: " + numCards + ", " + Arrays.toString(cards) + ")";
    }
}
