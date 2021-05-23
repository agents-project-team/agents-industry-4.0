package agents.product;

enum OrderStatus {
    inProgress,
    Completed
}

public class ProductOrder {

	private static int orderCounter = 0;

	private int orderId;
    //Might make a product id its own type
	private String productId;

	private int productAmount;
    //Simulation will create orders with different priorities based on other parameters
	private int orderPriority;

	private OrderStatus orderStatus;

	public ProductOrder() {

	}

	public ProductOrder(String id, int amount, int priority) {
		this.productId = id;
        productAmount = amount;
        orderPriority = priority;
        orderStatus = OrderStatus.inProgress;
		this.orderId = ++orderCounter;
    }

    public void CompleteOrder() {
        orderStatus = OrderStatus.Completed;
    }

	public static int getOrderCounter() {
		return orderCounter;
	}

	public static void setOrderCounter(int orderCounter) {
		ProductOrder.orderCounter = orderCounter;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public int getProductAmount() {
		return productAmount;
	}

	public void setProductAmount(int productAmount) {
		this.productAmount = productAmount;
	}

	public int getOrderPriority() {
		return orderPriority;
	}

	public void setOrderPriority(int orderPriority) {
		this.orderPriority = orderPriority;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}
}
