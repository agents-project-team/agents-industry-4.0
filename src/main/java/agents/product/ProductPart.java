package agents.product;

public class ProductPart {

    private int partId;

	private String type;

	public ProductPart() {

	}

    public ProductPart(String type, int id){
        this.type = type;
        this.partId = id;
    }

	public int getPartId() {
		return partId;
	}

	public void setPartId(int partId) {
		this.partId = partId;
	}

    public String getType() { return type; }

	public void setType(String type) {
		this.type = type;
	}
}
