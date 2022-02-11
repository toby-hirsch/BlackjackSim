import java.util.Arrays;
import java.util.HashMap;

public abstract class LinearStrategy implements Strategy {

    int[] feats;
    int numCards;
    BasicStrategy basic;
    //Aces are stored in the tenth slot here for dealer hand as opposed to the first slot in other places
    double[][][][] hardSolutions; //Stores solutions to spots where player has unpaired hard hand from 2 - 16 (2 - 8 are needed for pair splitting calculations)
                                                              //First dimension is player hand, second is dealer hand, third is action, fourth is feature coefficients
                                                              //hardSolutions[a][b][c][d] gives the coefficient for the dth feature of the cth action for the spot where the dealer is showing (c + 2) and player has hard (a + 8)
    double[][][][] softSolutions;
    double[][][] pairSplitting;  //EV predicition for pair splitting; 0 is AA, 9 is TT
    
    public LinearStrategy() {
        basic = new BasicStrategy();
    }
    
    public void optimize(Scenario s) {
        Simulator sim = new Simulator(3, 15);
        int epoch_size = 128;
        double step_size = 0.05;
        int iter = 0;
        Shoe shoe = new Shoe();
        double cost = 100;
        
        if (s.pHand.pair()) {
            Hand np;
            if (s.pHand.soft()) np = new Hand(11, true);
            else np = new Hand(s.pHand.val() / 2, false);
            optimize(new Scenario(np, s.dHand));
        }
        
        double[][] coefs = s.pHand.soft() ? softSolutions[s.pHand.val() - 12][s.dHand.val() - 2] : hardSolutions[s.pHand.val() - 2][s.dHand.val() - 2];
        
        while (iter < 500) {
            cost = 0;
            if (iter % 100 == 0) {
                System.out.println(iter + " | Step size: " + step_size + "\tValues: " + Arrays.deepToString(coefs));
            }
            for (int action = 0; action < 3; ++action) {
                double[] delta = new double[11];
                for (int i = 0; i < epoch_size; i++) {
                    if (i % 4 == 3) shoe.random(3);
                    else if (i % 3 == 2) {
                        shoe = new Shoe(3);
                        int rand = (int) (Math.random() * 80);
                        for (int j = 0; j < rand; j++) shoe.dealCard();
                    } else {
                        shoe = new Shoe(3);
                    }
                    update(shoe, 3);
                    
                    double actionEV = sim.calcActionEV(s.pHand, s.dHand, shoe, action);
                    double guess = guessActionEV(s, action);
                    //if (action == BJ.DOUBLE) System.out.println("actual: " + actionEV + ", guess: " + guess);
                    double err = actionEV - guess;
                    cost += err;
                    for (int j = 0; j < 10; j++) {
                        delta[j] += err * sigmoid(feats[j] * 52 / numCards);
                    }
                    delta[10] += err;
                    
                    /*if (hitEV > standEV && action == BJ.STAND) {
                        badStands++;
                        double err = hitEV - standEV;
                        cost += err;
                        for (int j = 0; j < 10; j++) {
                            delta[j] += err * feats[j] * 52 / numCards;
                        }
                        delta[10] += err;
                    } else if (standEV > hitEV && action == BJ.HIT) {
                        badHits++;
                        double err = standEV - hitEV;
                        cost += err;
                        for (int j = 0; j < 10; j++) {
                            delta[j] -= err * feats[j] * 52 / numCards;
                        }
                        delta[10] -= err;
                    }*/
                    
                }
                for (int j = 0; j < 11; j++) {
                    coefs[action][j] += step_size * delta[j] / epoch_size;
                }
            }
            
            //step_size *= 0.999;
            if (iter % 100 == 0) {
                System.out.println("Inaccuracy per hand: " + (cost / epoch_size / 3));
            }
            ++iter;
            
        }
        
        System.out.println(Arrays.deepToString(coefs));
        sim.run(s.pHand, s.dHand, new Strategy[] {this, new BasicStrategy()});
    }
    
    @Override
    public int solve(Scenario s) {
        if (s.pHand.val() < 8) return BJ.HIT;
        if (s.pHand.val() > 17 && !s.pHand.soft()) return BJ.STAND;
        else {
            double EV = guessActionEV(s, BJ.STAND);
            int action = BJ.STAND;
            for (int i = 1; i < 4; i++) {
                double nEV = guessActionEV(s, i);
                if (nEV > EV) {
                    EV = nEV;
                    action = i;
                }
            }
            return action;
            
        }        
    }
    
    private double guessActionEV(Scenario s, int action) {
        double res = 0;
        double[] coefs;
        
        if (action == BJ.SPLIT) {
            if (!s.pHand.pair()) return Integer.MIN_VALUE;
            return 2 * guessActionEV(new Scenario(s.pHand.split(), s.dHand), BJ.HIT);
        }
        if (s.pHand.soft()) coefs = softSolutions[s.pHand.val() - 8][s.dHand.val() - 2][action];
        else coefs = hardSolutions[s.pHand.val() - 2][s.dHand.val() - 2][action];
        
        for (int i = 0; i < coefs.length - 1; i++) {
            res += feats[i] * 52 / numCards * coefs[i];
        }
        res += coefs[coefs.length - 1];
        
        return res;
    }

    private double sigmoid(double d) {
        return 1 / (1 + Math.exp(-d)) - 0.5;
    }
    
}
