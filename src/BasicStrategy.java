import java.util.HashMap;

public class BasicStrategy implements Strategy {

    @Override
    public void update(int card) {}

    @Override
    public int solve(Scenario s) {
        int p = s.pHand.val();
        int d = s.dHand.val();
        if (s.pHand.pair()) {
            switch(p) {
                case 20: return BJ.STAND;
                case 18: return d == 7 || d == 10 || d == 11 ? BJ.STAND : BJ.SPLIT;
                case 16: return BJ.SPLIT;
                case 14: return d < 8 ? BJ.SPLIT : BJ.HIT;
                case 12: return !s.pHand.soft() && (d == 2 || d > 6) ? BJ.HIT : BJ.SPLIT;
                case 10: return d < 10 ? BJ.DOUBLE : BJ.HIT;
                case 8: return BJ.HIT;
                case 6: return d < 4 || d > 7 ? BJ.HIT : BJ.SPLIT;
                case 4: return d < 4 || d > 7 ? BJ.HIT : BJ.SPLIT;
            }
        }
        
        if (s.pHand.soft()) {
            switch(p) {
                case 20: return BJ.STAND;
                case 19: return d == 6 ? BJ.DOUBLE : BJ.STAND;
                case 18: 
                    if (d < 7) return BJ.DOUBLE;
                    if (d < 9) return BJ.STAND;
                    return BJ.HIT;
                case 17: return d == 2 || d > 6 ? BJ.HIT : BJ.DOUBLE;
                case 16: return d < 4 || d > 6 ? BJ.HIT : BJ.DOUBLE;
                case 15: return d < 4 || d > 6 ? BJ.HIT : BJ.DOUBLE;
                case 14: return d < 5 || d > 6 ? BJ.HIT : BJ.DOUBLE;
                case 13: return d < 5 || d > 6 ? BJ.HIT : BJ.DOUBLE;
            }
        }
        
        if (p > 16) return BJ.STAND;
        if (p < 9) return BJ.HIT;
        
        switch(p) {
            
        }
    }

    @Override
    public void reset(int shoeSize) {}

    @Override
    public String name() {
        return "Basic Strategy";
    }

    @Override
    public void update(Shoe shoe, int shoeSize) {}

    @Override
    public void optimize(Scenario s) {
        // TODO Auto-generated method stub
        
    }

}
