package agents.product;
import agents.workers.machines.MachineType;

import java.util.HashMap;
import java.util.Map;

//Blueprint class
public class ProductPlan {

    private int Id;

	private int priority;

	private int amount;

	private Map<MachineType, PartPlan> planParts = new HashMap<>();

	public ProductPlan() { }

    public ProductPlan(ProductOrder order) {
        this.Id = order.getOrderId();
        this.priority = order.getOrderPriority();
        this.amount = order.getProductAmount();
        createProductPlan(order.getProductId());
    }

    public ProductPlan(ProductPlan plan){
        this.Id = plan.Id;
        this.priority = plan.priority;
        this.amount = plan.amount;
        this.planParts = new HashMap<>(plan.planParts);
    }

	private void createProductPlan(String productId) {
        String[] types = productId.split("-");
        for(String var : types) {
            switch (var.charAt(0)) {
                case 'A':
					planParts.put(MachineType.SurfaceFabric, new PartPlan(this.Id, var.substring(1), this.amount));
                    break;
                case 'B':
					planParts.put(MachineType.InnerFabric, new PartPlan(this.Id, var.substring(1), this.amount));
                    break;
                case 'C':
					planParts.put(MachineType.DetailFabric, new PartPlan(this.Id, var.substring(1), this.amount));
                    break;
                case 'D':
					planParts.put(MachineType.Sole, new PartPlan(this.Id, var.substring(1), this.amount));
                    break;
                case 'E':
					planParts.put(MachineType.Outsole, new PartPlan(this.Id, var.substring(1), this.amount));
                    break;
            }
        }
    }

    public void decreasePartPlanAmount(MachineType key){
        this.planParts.get(key).decreaseCurrentAmount();
        this.decreaseTotalAmount();
    }

    public void decreaseTotalAmount(){
        double totalCurrent = 0;
        double total = 0;
        for(PartPlan part : planParts.values()){
            total += part.getTotalAmount();
            totalCurrent += part.getCurrentAmount();
        }
        if(this.amount > totalCurrent/total){
            this.amount--;
        }
    }

    public boolean decreaseAllAmounts(){
        for(PartPlan p : planParts.values()){
            p.decreaseCurrentAmount();
        }
        this.amount--;
		return this.amount == 0;
    }

    public void setAmount(int amount){
        this.amount = amount;
    }
    public void setPriority(int priority){ this.priority = priority; }
    public void setId(int id) { this.Id = id; }
    public void setPlanParts(Map<MachineType, PartPlan> planParts){ this.planParts = planParts; }

    public int getAmount(){ return this.amount; }
    public int getPriority(){ return this.priority; }
    public int getId(){ return this.Id; }
    public Map<MachineType, PartPlan> getPlanParts(){ return this.planParts; }

	@Override
	public String toString() {
		return "ProductPlan{" +
				"Id=" + Id +
				", priority=" + priority +
				", amount=" + amount +
				", planParts=" + planParts +
				'}';
	}
}
