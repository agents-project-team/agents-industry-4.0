package agents.product;

enum OrderStatus {
    inProgress,
    Completed
}

public class ProductOrder {
    public static int orderCounter = 0;
    public final int orderId;
    //Might make a product id its own type
    public final String productId;
    public final int productAmount;
    //Simulation will create orders with different priorities based on other parameters
    public final int orderPriority;
    public OrderStatus orderStatus;

    public ProductOrder(String id, int amount, int priority){
        productId = id;
        productAmount = amount;
        orderPriority = priority;
        orderStatus = OrderStatus.inProgress;
        orderId = ++orderCounter;
    }

    public void CompleteOrder() {
        orderStatus = OrderStatus.Completed;
    }
}
