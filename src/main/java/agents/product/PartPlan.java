package agents.product;

//Part blueprint
public class PartPlan {
    private final int Id;
    private final String partType;

    public PartPlan(int Id, String plan){
        this.Id = Id;
        this.partType = plan;
    }

    public int getId() { return Id; }
    public String getPartType() { return partType; }
}
