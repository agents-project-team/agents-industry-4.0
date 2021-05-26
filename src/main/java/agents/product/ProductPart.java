package agents.product;

public class ProductPart {
    private String type;
    private int partId;

    public ProductPart(String type){
        this.type = type;
        partId = 1;
    }

    public String getType() { return type; }
}
