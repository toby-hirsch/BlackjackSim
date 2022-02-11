import java.util.Objects;

public class Scenario {
    Hand pHand, dHand;
    
    public Scenario(Hand p, Hand d) {
        pHand = p;
        dHand = d;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        Scenario s = (Scenario) obj;
        return pHand.val() == s.pHand.val() && dHand.val() == s.dHand.val() && pHand.soft() == s.pHand.soft() && dHand.soft() == s.dHand.soft();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(pHand.val(), dHand.val(), pHand.soft(), dHand.soft());
    }
}
