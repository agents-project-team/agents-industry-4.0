package agents.product;
import agents.workers.machines.MachineType;
import java.util.Map;

//Blueprint class
public class ProductPlan {

    private final int Id;

	private final int priority;

	private int amount;

	private Map<MachineType, String> planParts;

    public ProductPlan(ProductOrder order){
        this.Id = order.getOrderId();
        this.priority = order.getOrderPriority();
        this.amount = order.getProductAmount();
        createProductPlan(order.getProductId());
    }

	private void createProductPlan(String productId) {
        String[] types = productId.split("-");
        for(String var : types) {
            switch (var.charAt(0)) {
                case 'A':
					planParts.put(MachineType.SurfaceFabric, var.substring(1));
                    break;
                case 'B':
					planParts.put(MachineType.InnerFabric, var.substring(1));
                    break;
                case 'C':
					planParts.put(MachineType.DetailFabric, var.substring(1));
                    break;
                case 'D':
					planParts.put(MachineType.Sole, var.substring(1));
                    break;
                case 'E':
					planParts.put(MachineType.Outsole, var.substring(1));
                    break;
            }
        }
    }

    public void updateAmount(int amount){
        this.amount = amount;
    }
    public int getAmount(){ return this.amount; }
    public int getPriority(){ return this.priority; }
    public int getId(){ return this.Id; }
    public Map<MachineType, String> getPlanParts(){ return this.planParts; }
}
