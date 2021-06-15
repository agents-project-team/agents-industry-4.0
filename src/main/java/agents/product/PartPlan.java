package agents.product;

import java.io.Serializable;

public class PartPlan implements Serializable {

	private int Id;
    private int totalAmount;
    private int currentAmount;
    private String partType;
    private double seconds;

	public PartPlan() { }

    public PartPlan(int id, String partType, int totalAmount, double seconds){
        this.Id = id;
        this.partType = partType;
        this.totalAmount = totalAmount;
        this.currentAmount = totalAmount;
        this.seconds = seconds;
    }

    public PartPlan(PartPlan plan){
        this.Id = plan.Id;
        this.partType = plan.partType;
        this.totalAmount = plan.totalAmount;
        this.currentAmount = plan.currentAmount;
        this.seconds = plan.seconds;
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
	public double getSeconds() {
		return seconds;
	}
	public void setSeconds(double seconds) {
		this.seconds = seconds;
	}

    public void decreaseCurrentAmount(){
        this.currentAmount--;
    }

    public String toEventString() {
        return "PartPlan{ " +
                "planId=" + Id +
                "partId=" + partType +
                " }";
    }
}
