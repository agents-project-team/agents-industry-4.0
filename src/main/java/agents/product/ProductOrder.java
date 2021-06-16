package agents.product;

public class ProductOrder {

	private static int orderCounter = 0;

	private int orderId;
    //Might make a product id its own type
	private String productId;

	private int productAmount;
    //Simulation will create orders with different priorities based on other parameters
	private int orderPriority;

	public ProductOrder() {

	}

	public ProductOrder(String id, int amount, int priority) {
		this.productId = id;
        productAmount = amount;
        orderPriority = priority;
		this.orderId = ++orderCounter;
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

	@Override
	public String toString() {
		return "Order{" +
				"orderId=" + orderId +
				", productAmount=" + productAmount +
				", productPriority=" + orderPriority +
				", productId=" + productId +
				'}';
	}
}
