package agents.product;
import agents.configs.SimulationConfig;
import agents.managers.PlanStatus;
import agents.workers.machines.MachineType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//Blueprint class
public class ProductPlan {

    private PlanStatus status;

    private int Id;

	private int priority;

	private int currentAmount;

	private int totalAmount;

	private Map<MachineType, PartPlan> planParts = new HashMap<>();

	public ProductPlan() { }

    public ProductPlan(ProductOrder order) {
	    this.status = PlanStatus.inProgress;
        this.Id = order.getOrderId();
        this.priority = order.getOrderPriority();
        this.currentAmount = order.getProductAmount();
        this.totalAmount = order.getProductAmount();
        createProductPlan(order.getProductId());
    }

    public ProductPlan(ProductPlan plan){
        this.Id = plan.Id;
        this.priority = plan.priority;
        this.currentAmount = plan.currentAmount;
        this.totalAmount = plan.totalAmount;
        this.planParts = new HashMap<>(plan.planParts);
    }

	private void createProductPlan(String productId) {
        String[] types = productId.split("-");
        for(String var : types) {
            switch (var.charAt(0)) {
                case 'A':
					planParts.put(MachineType.SurfaceFabric, new PartPlan(this.Id, var, this.totalAmount, SimulationConfig.SECONDS_TO_CREATE_SURFACE_FABRIC));
                    break;
                case 'B':
					planParts.put(MachineType.InnerFabric, new PartPlan(this.Id, var, this.totalAmount, SimulationConfig.SECONDS_TO_CREATE_INNER_FABRIC));
                    break;
                case 'C':
					planParts.put(MachineType.DetailFabric, new PartPlan(this.Id, var, this.totalAmount, SimulationConfig.SECONDS_TO_CREATE_DETAIL_FABRIC));
                    break;
                case 'D':
					planParts.put(MachineType.Sole, new PartPlan(this.Id, var, this.totalAmount, SimulationConfig.SECONDS_TO_CREATE_SOLE));
                    break;
                case 'E':
					planParts.put(MachineType.Outsole, new PartPlan(this.Id, var, this.totalAmount, SimulationConfig.SECONDS_TO_CREATE_OUTSOLE));
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
        double amountCheck = (totalCurrent/total)*this.totalAmount;
        if(this.currentAmount > Math.ceil(amountCheck)){
            this.currentAmount--;
        }
        if(this.currentAmount == 0) this.status = PlanStatus.Completed;
    }

	public void decreaseAllAmounts(){
		for(PartPlan p : planParts.values()){
			p.decreaseCurrentAmount();
		}
		this.currentAmount--;
	}

	public void setStatus(PlanStatus status){ this.status = status; }
    public void setCurrentAmount(int currentAmount){ this.currentAmount = currentAmount; }
	public void setTotalAmount(int totalAmount){
        this.totalAmount = totalAmount;
    }
    public void setPriority(int priority){ this.priority = priority; }
    public void setId(int id) { this.Id = id; }
    public void setPlanParts(Map<MachineType, PartPlan> planParts){ this.planParts = planParts; }

    public PlanStatus getStatus(){ return this.status; }
    public int getCurrentAmount(){ return this.currentAmount; }
    public int getTotalAmount(){ return this.totalAmount; }
    public int getPriority(){ return this.priority; }
    public int getId(){ return this.Id; }
    public Map<MachineType, PartPlan> getPlanParts(){ return this.planParts; }

	@Override
	public String toString() {
		return "ProductPlan{" +
				"Id=" + Id +
				", priority=" + priority +
				", Current Amount=" + currentAmount +
                ", Total Amount = "+ totalAmount +
				", planParts=" + planParts +
				'}';
	}
}
