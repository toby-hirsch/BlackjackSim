import java.util.Arrays;
import java.util.ArrayList;

/*
 * Doesn't accommodate player blackjack since there's no decision making if player is dealt blackjack
 */
public class Simulator {
    private int shoeSize, stoppingPoint;
    
    public static void main(String[] args) throws Exception {
        boolean[] t = new boolean[] {
                false,  //A
                false,  //2
                false,   //3
                false,   //4
                false,   //5
                false,   //6
                false,   //7
                false,   //8
                false,   //9
                false    //T
        };
        boolean[] alltrue = new boolean[10];
        for (int i = 0; i < 10; i++)
            alltrue[i] = true;
        new AllCards(alltrue).optimize(BJ.SIXTEEN_TEN);
        
        Simulator s = new Simulator(3, 15);
        s.fullGameSim(new Strategy[] {new BasicStrategy(), new AllCards(alltrue)});
        //s.run(new Hand(12, false), new Hand(4, false), 
          //      new Strategy[] {new BasicStrategy(), new HiLo(s.getShoeSize())});
    }
    
    public Simulator(int shoe, int stop) {
        shoeSize = shoe;
        stoppingPoint = stop;
    }
    
    public double[] run(Hand pHand, Hand dHand, Strategy[] strats) {
        Scenario scen = new Scenario(pHand, dHand);
        int numStrats = strats.length;
        double hitEV = 0, standEV = 0, optimalEV = 0;
        double[] stratEVs = new double[numStrats];
        
        double[] cardHitWeights = new double[10], cardStandWeights = new double[10];
        int totalStands = 0, totalHits = 0;
        
        long t = System.currentTimeMillis();
        
        int trials = 10000;
        for (int i = 0; i < trials; i++) {
            Shoe shoe = new Shoe();
            shoe.random(getShoeSize());
            for (Strategy s : strats)
                s.update(shoe, getShoeSize());
            
            /*double standE = calcActionEV(pHand, dHand, shoe, BJ.STAND);
            double hitE = calcActionEV(pHand, dHand, shoe, BJ.HIT);
            
            if (hitE > standE) {
                ++totalHits;
                for (int j = 0; j < 9; j++)
                    cardHitWeights[j] += shoe.getProb(j);
                cardHitWeights[9] += shoe.getProb(9) / 4;
            }
            else {
                ++totalStands;
                for (int j = 0; j < 9; j++)
                    cardStandWeights[j] += shoe.getProb(j);
                cardStandWeights[9] += shoe.getProb(9) / 4;
            }
            
            hitEV += hitE;
            standEV += standE;
            optimalEV += Math.max(standE, hitE);
            for (int j = 0; j < numStrats; j++)
                stratEVs[j] += strats[j].solve(scen) == BJ.STAND ? standE : hitE;*/
            
            double[] actionEVs = new double[4];
            for (int j = 0; j < 4; j++) {
                actionEVs[j] = calcActionEV(pHand, dHand, shoe, j);
            }
            hitEV += actionEVs[1];
            standEV += actionEVs[0];
            double optEV = Arrays.stream(actionEVs).max().getAsDouble();
            optimalEV += optEV;
            for (int j = 0; j < numStrats; j++)
                stratEVs[j] += actionEVs[strats[j].solve(scen)];
                
        }
        
        //STD and STE calculations are wrong for many reasons but useful
        System.out.println("Runtime: " + (System.currentTimeMillis() - t));
        int numHands = trials;//(getShoeSize() * 52 - stoppingPoint) * trials;
        
        System.out.println(hitEV / trials);
        System.out.println(standEV / trials);
        System.out.println("Optimal Play \t| EV: " + (optimalEV / numHands));
        for (int i = 0; i < numStrats; i++)
            System.out.println(strats[i].name() + " \t| EV: " + (stratEVs[i] / numHands));
        
        //Adjusting weights so that a value of 1 is average
        /*System.out.print("Card hit weights:\tA: " + ((double) Math.round(130000 * cardHitWeights[0] / totalHits)) / 10000);
        for (int i = 1; i < 10; i++)
            System.out.print("\t" + (i + 1) + ": " + ((double) Math.round(130000 * cardHitWeights[i] / totalHits)) / 10000);
        
        System.out.print("\nCard stand weights:\tA: " + ((double) Math.round(130000 * cardStandWeights[0] / totalStands)) / 10000);
        for (int i = 1; i < 10; i++)
            System.out.print("\t" + (i + 1) + ": " + ((double) Math.round(130000 * cardStandWeights[i] / totalStands)) / 10000);*/
        
        return stratEVs;
        
    }
    
    //TODO: add support for other rule sets; currently using dealer hits soft 17, no DAS, no surrender, dealer peek
    public double[] fullGameSim(Strategy[] strats) { 
        int numStrats = strats.length;
        double[] stratEVs = new double[numStrats];
        double optimalEV = 0;
        Shoe shoe = new Shoe(getShoeSize());
        int trials = 10;
        int[] cards = new int[3];
        for (int i = 0; i < trials; i++) {
            System.out.println("trial " + i);
            while (shoe.getNumCards() > stoppingPoint) {
                for (int j = 0; j < 3; j++)
                    cards[j] = shoe.dealCard();
                Hand dHand = new Hand(cards[0]);
                Hand pHand = new Hand(cards[1], cards[2]);
                Scenario s = new Scenario(pHand, dHand);
                double[] actionEVs = new double[4];
                for (int j = 0; j < 4; j++) {
                    actionEVs[j] = calcActionEV(pHand, dHand, shoe, j);
                }
                double optEV = Arrays.stream(actionEVs).max().getAsDouble();
                optimalEV += optEV;
                for (int j = 0; j < numStrats; j++) {
                    strats[j].optimize(s);
                    stratEVs[j] += actionEVs[strats[j].solve(s)];
                }
            }
        }
        int numHands = trials * (shoeSize * 52 - stoppingPoint);
        System.out.println("Optimal Play \t| EV: " + (optimalEV / numHands));
        for (int i = 0; i < numStrats; i++)
            System.out.println(strats[i].name() + " \t| EV: " + (stratEVs[i] / numHands));
        
        return stratEVs;
        
    }
    
    public double calcActionEV(Hand pHand, Hand dHand, Shoe shoe, int action) {
        double EV = 0;
        double mult = 1;
        if (dHand.val() == 10) {
            EV -= shoe.getProb(0); //Player loses if dealer has a natural
            mult -= shoe.getProb(0);
        } else if (dHand.val() == 11) {
            EV -= shoe.getProb(9);
            mult -= shoe.getProb(9);
        }
        switch (action) {
            case BJ.STAND:  EV += mult * calcStandEV(pHand, dHand, shoe, true);
                            break;
            case BJ.HIT:    EV += mult * calcHitEV(pHand, dHand, shoe);
                            break;
            case BJ.DOUBLE: EV += mult * calcDoubleEV(pHand, dHand, shoe);
                            break;
            case BJ.SPLIT:  EV += mult * calcSplitEV(pHand, dHand, shoe);
                            break;
            default:        return Integer.MIN_VALUE;
        }
        return EV;
    }
    
    public double calcStandEV(Hand pHand, Hand dHand, Shoe shoe, boolean peeked) {
        double denom = 1;
        if (pHand.val() > 21) {
            return -1;
        }
        if (dHand.val() > 21) {
            if (dHand.soft()) {
                dHand.harden();
                return calcStandEV(pHand, dHand, shoe, false);
            } else {
                return 1;
            }
        }
        
        if (dHand.val() > 17 || dHand.val() == 17 && !dHand.soft()) {
            if (dHand.val() > pHand.val()) {
                return -1;
            }
            else if (dHand.val() < pHand.val()) {
                return 1;
            }
            else {
                return 0;
            }
        } else {
            double EV = 0;
            double cardProb = shoe.getProb(0);
            if (cardProb != 0) {
                if (dHand.val() == 10 && peeked) denom -= cardProb;
                else {
                    Hand d = new Hand(dHand);
                    d.addAce();
                    Shoe s = new Shoe(shoe);
                    s.removeCard(0);
                    EV += cardProb * calcStandEV(pHand, d, s, false);
                }
            }
            
            for (int card = 1; card < 9; card++) {
                cardProb = shoe.getProb(card);
                if (cardProb == 0) {
                    continue;
                }
                
                Hand d = new Hand(dHand);
                d.inc(card + 1);
                Shoe s = new Shoe(shoe);
                s.removeCard(card);
                EV += cardProb * calcStandEV(pHand, d, s, false);
            }
            
            cardProb = shoe.getProb(9);
            if (cardProb != 0) {
                if (dHand.val() == 11 && peeked) denom -= cardProb;
                else {
                    Hand d = new Hand(dHand);
                    d.inc(10);
                    Shoe s = new Shoe(shoe);
                    s.removeCard(9);
                    EV += cardProb * calcStandEV(pHand, d, s, false);
                }
            }
            
            return EV / denom;
        }
    }
    
    public double calcHitEV(Hand pHand, Hand dHand, Shoe shoe) {
        if (pHand.val() > 21) {
            if (pHand.soft()) {
                pHand.harden();
                return Math.max(calcStandEV(pHand, dHand, shoe, true), calcHitEV(pHand, dHand, shoe));
            }
            return -1;
        }
        
        if (pHand.val() > 16 && !pHand.soft()) {
            return calcStandEV(pHand, dHand, shoe, true);
        }
        
        double EV = 0;
        double cardProb = shoe.getProb(0);
        
        if (cardProb != 0) {
            Hand p = new Hand(pHand);
            p.addAce();
            Shoe s = new Shoe(shoe);
            s.removeCard(0);
            EV += cardProb * Math.max(calcStandEV(p, dHand, s, true), calcHitEV(p, dHand, s));
        }
        
        for (int card = 1; card < 10; card++) {
            cardProb = shoe.getProb(card);
            if (cardProb == 0) {
                continue;
            }
            
            Hand p = new Hand(pHand);
            p.inc(card + 1);
            Shoe s = new Shoe(shoe);
            s.removeCard(card);
            EV += cardProb * Math.max(calcStandEV(p, dHand, s, true), calcHitEV(p, dHand, s));
            //if (parent) System.out.println("EV after " + card + ": " + EV);
        }
        
        return EV;
    }

   
    public double calcDoubleEV(Hand pHand, Hand dHand, Shoe shoe) {
        double EV = 0;
        double cardProb = shoe.getProb(0);
        
        if (cardProb != 0) {
            Hand p = new Hand(pHand);
            p.addAce();
            Shoe s = new Shoe(shoe);
            s.removeCard(0);
            EV += 2 * cardProb * calcStandEV(p, dHand, s, true);
        }
        
        for (int card = 1; card < 10; card++) {
            cardProb = shoe.getProb(card);
            if (cardProb == 0) {
                continue;
            }
            
            Hand p = new Hand(pHand);
            p.inc(card + 1);
            Shoe s = new Shoe(shoe);
            s.removeCard(card);
            EV += 2 * cardProb * calcStandEV(p, dHand, s, true);
        }
        
        return EV;
    }
    
    public double calcSplitEV(Hand pHand, Hand dHand, Shoe shoe) {
        if (!pHand.pair()) return Integer.MIN_VALUE;
        if (pHand.soft())
            return 2 * calcHitEV(new Hand(11, true), dHand, shoe);
        return 2 * calcHitEV(new Hand(pHand.val() / 2, false), dHand, shoe);
    }
    
    public int getShoeSize() {
        return shoeSize;
    }
}
