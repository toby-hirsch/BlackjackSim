
public interface Strategy {
    
    public String name();
    
    public void optimize(Scenario s);
    
    public void reset(int shoeSize);
    
    public int solve(Scenario s);
    
    public void update(int card);
    
    public void update(Shoe shoe, int shoeSize);
    
}
