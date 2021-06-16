package agents.product;

import java.io.Serializable;

public class ProductPart implements Serializable {

	private String partId;

	public ProductPart() {

	}

    public ProductPart(String type){
		this.partId = type;
    }

    public String getType() { return partId; }

	public void setType(String partId) {
		this.partId = partId;
	}

	@Override
	public String toString() {
		return "ProductPart{" +
				" partId='" + partId + '\'' +
				'}';
	}
}
