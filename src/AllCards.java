import java.util.Arrays;

public class AllCards extends LinearStrategy {

    boolean[] tracking;
    
    public AllCards(boolean[] t) throws Exception {
        if (t.length != 10)
            throw new Exception();
        tracking = t;
        feats = new int[10];
        hardSolutions =  new double[15][10][3][11];
        softSolutions = new double[9][10][3][11];
        pairSplitting = new double[10][10][11];
    }
    
    @Override
    public String name() {
        return "All Cards Linear";
    }

    @Override
    public void reset(int shoeSize) {
        feats = new int[10];
        numCards = shoeSize * 52;
    }

    @Override
    public void update(int card) {
        --numCards;
        if (tracking[card])
            feats[card]++;
    }

    @Override
    public void update(Shoe shoe, int shoeSize) {
        for (int i = 0; i < 9; i++) {
            if (tracking[i])
                feats[i] = shoeSize * 4 - shoe.getCount(i);
        }
        if (tracking[9])
            feats[9] = shoeSize * 16 - shoe.getCount(9);
        numCards = shoe.getNumCards();
    }

}
