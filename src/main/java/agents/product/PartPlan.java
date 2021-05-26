package agents.product;

//Part plan
public class PartPlan {

	private int Id;
    private int totalAmount;
    private int currentAmount;
    private String partType;

    public PartPlan() { }

    public PartPlan(int id, String partType, int totalAmount){
        this.Id = id;
        this.partType = partType;
        this.totalAmount = totalAmount;
        this.currentAmount = totalAmount;
    }

    public PartPlan(PartPlan plan){
        this.Id = plan.Id;
        this.partType = plan.partType;
        this.totalAmount = plan.totalAmount;
        this.currentAmount = plan.currentAmount;
    }

    public void setId(int id){ this.Id = id; }
    public void setPartType(String partType) { this.partType = partType; }
    public void setCurrentAmount(int amount) { this.currentAmount = amount; }
    public void setTotalAmount(int amount) { this.totalAmount = amount; }

    public void setAmounts(int amount){
        this.currentAmount = amount;
        this.totalAmount = amount;
    }

    public int getId() { return Id; }
    public String getPartType() { return partType; }
    public int getCurrentAmount() { return currentAmount; }
    public int getTotalAmount() { return totalAmount; }

    public void decreaseCurrentAmount(){
        this.currentAmount--;
    }
}
