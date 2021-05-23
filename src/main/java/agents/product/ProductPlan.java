package agents.product;


import agents.workers.MachineType;
import java.util.Map;

//Blueprint class
public class ProductPlan {
    public final int Id;
    public final int priority;
    public int amount;
    public Map<MachineType, String>  planParts;

    public ProductPlan(ProductOrder order){
        this.Id = order.orderId;
        this.priority = order.orderPriority;
        this.amount = order.productAmount;
        createProductPlan(order.productId);
    }

    public void createProductPlan(String productId){
        String[] types = productId.split("-");
        for(String var : types) {
            switch (var.charAt(0)) {
                case 'A':
                    planParts.put(MachineType.SurfaceFabric, var.substring(1, var.length()));
                    break;
                case 'B':
                    planParts.put(MachineType.InnerFabric, var.substring(1, var.length()));
                    break;
                case 'C':
                    planParts.put(MachineType.DetailFabric, var.substring(1, var.length()));
                    break;
                case 'D':
                    planParts.put(MachineType.Sole, var.substring(1, var.length()));
                    break;
                case 'E':
                    planParts.put(MachineType.Outsole, var.substring(1, var.length()));
                    break;
            }
        }
    }
}
