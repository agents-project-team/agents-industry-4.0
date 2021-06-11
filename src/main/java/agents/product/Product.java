package agents.product;

import java.util.ArrayList;
import java.util.List;

public class Product {

    private int productId;
    private int productAmount;
    private List<ProductPart> productParts = new ArrayList<>();

    public Product() { }

    public Product(int pId, int pAmount, List<ProductPart> pParts){
        this.productId = pId;
        this.productAmount = pAmount;
        this.productParts = pParts;
    }

    public void setProductId(int pId) { this.productId = pId; }
    public void setProductAmount(int pAmount) { this.productAmount = pAmount; }
    public void setProductParts(List<ProductPart> pParts) { this.productParts = pParts; }

    public void increaseAmount(int amount){
        this.productAmount += amount;
    }

    public int getProductId(){ return this.productId; }
    public int getProductAmount(){ return  this.productAmount; }
    public List<ProductPart> getProductParts(){ return this.productParts; }

	@Override
	public String toString() {
		return "P{" +
				"productId=" + productId +
				", productAmount=" + productAmount +
				'}';
	}
}
