package agents.workers.assemblers;

import agents.product.ProductPart;
import agents.product.ProductPlan;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AssemblerState implements Serializable {

	private List<ProductPlan> currentPlans = new ArrayList<>();

	private List<ProductPart> storedParts = new ArrayList<>();

	public AssemblerState() {
	}

	public AssemblerState(List<ProductPlan> currentPlans, List<ProductPart> storedParts) {
		this.currentPlans = currentPlans;
		this.storedParts = storedParts;
	}

	public List<ProductPlan> getCurrentPlans() {
		return currentPlans;
	}

	public List<ProductPart> getStoredParts() {
		return storedParts;
	}

	@Override
	public String toString() {
		return "AssemblerState{" +
				"currentPlans=" + currentPlans +
				", storedParts=" + storedParts +
				'}';
	}
}
