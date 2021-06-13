package agents.product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Product {

    private String productId;
    private int productAmount;
    private List<ProductPart> productParts = new ArrayList<>();

    public Product() { }

    public Product(int pAmount, List<ProductPart> pParts){
        this.productId = createProductId(pParts);
        this.productAmount = pAmount;
        this.productParts = pParts;
    }

    public void setProductId(String pId) { this.productId = pId; }
    public void setProductAmount(int pAmount) { this.productAmount = pAmount; }
    public void setProductParts(List<ProductPart> pParts) { this.productParts = pParts; }

    public void increaseAmount(int amount){
        this.productAmount += amount;
    }

    public String getProductId(){ return this.productId; }
    public int getProductAmount(){ return  this.productAmount; }
    public List<ProductPart> getProductParts(){ return this.productParts; }

    public String createProductId(List<ProductPart> pParts){
        Collections.sort(pParts, (part1, part2) -> part1.getType().compareTo(part2.getType()));
        String tmpId = "";
        for(ProductPart var : pParts){
            tmpId = tmpId+var.getType()+"-";
        }
        return tmpId.substring(0, tmpId.length() - 1);
    }

	@Override
	public String toString() {
        Collections.sort(productParts, (part1, part2) -> part1.getType().compareTo(part2.getType()));
        String parts = "";
        for(ProductPart part : productParts){
            parts = parts + part.getType() + " ";
        }
		return "P{" +
				"productId=" + productId +
				", productAmount=" + productAmount +
                ", parts=" + parts +
				'}';
	}
}
