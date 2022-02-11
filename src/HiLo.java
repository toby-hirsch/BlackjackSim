
public class HiLo extends LinearStrategy {
    private int count, numCards;
    
    public HiLo(int shoeSize) {
        this();
        numCards = shoeSize * 52;
    }
    
    public HiLo() { //should only be invoked if reset() will be called before first use; otherwise, pass a shoe size
        count = 0;
        feats = new int[1];
        solutions.put(BJ.SIXTEEN_TEN, new double[] {-1, 1});
    }

    @Override
    public String name() {
        return "HiLo";
    }
    
    @Override
    public void reset(int shoeSize) {
        count = 0;
        numCards = shoeSize * 52;
        feats = new int[0];
    }
    
    @Override
    public void update(int card) {
        if (card > 0 && card < 6) ++count;
        else if (card == 0 || card == 9) --count;
        --numCards;
    }

    @Override
    public void update(Shoe shoe, int shoeSize) {
        feats = new int[1];
        for (int i = 1; i < 6; i++) {
            feats[0] += shoeSize * 4 - shoe.getCount(i);
        }
        feats[0] -= shoeSize * 4 - shoe.getCount(0);
        feats[0] -= shoeSize * 16 - shoe.getCount(9);
        numCards = shoe.getNumCards();
    }

    
    
    
}
