package agents.workers.machines;

import agents.product.PartPlan;
import agents.product.ProductPart;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import agents.workers.Worker;
import agents.workers.assemblers.AssemblerType;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.Arrays;

public class MachineAgent extends Worker<PartPlan> {

	private String customState;

	private Object bluePrint;

	private int numberOfDetails;

	private AID assemblerId;

	private int seconds;

	private PartPlan currentPlan;

	@Override
	protected void setup() {
		super.setup();
		super.addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.PROPOSE) {
						Logger.process(getLocalName() + " replaces broken machine");

						ContainerID destination = new ContainerID();
						destination.setName("Main-Container");
						doMove(destination);
					} else if (msg.getPerformative() == ACLMessage.REQUEST) {
						PartPlan plan = JsonConverter.fromJsonString(msg.getContent(), PartPlan.class);
						currentPlan = plan;

						Logger.info(getLocalName() + " is creating a part");

						doWait(2000);

						ProductPart createdPart = new ProductPart(plan.getPartType(), plan.getId());

						AID receiverAssembler = getCurrentAssembler();
						System.out.println("for " + receiverAssembler);
						if (receiverAssembler != null) {
							ACLMessage msgToAssembler = new ACLMessage(ACLMessage.UNKNOWN);
							msgToAssembler.setProtocol("SPART");
							msgToAssembler.addReceiver(receiverAssembler);
							msgToAssembler.setContent(JsonConverter.toJsonString(createdPart));
							send(msgToAssembler);
						}

						ACLMessage msgToManager = new ACLMessage(ACLMessage.INFORM);
						msgToManager.addReceiver(getManagerId());
						msgToManager.setProtocol("FTASK");
						msgToManager.setContent(msg.getContent());
						send(msgToManager);
					}
				} else {
					block();
				}
			}
		});
	}

	@Override
	public PartPlan getUnfinishedTask() {
		return currentPlan;
	}

	private AID getCurrentAssembler() {
		ServiceDescription sd = new ServiceDescription();
		MachineType ownType = MachineType.getByName(getWorkerType());
		if (ownType == MachineType.DetailFabric || ownType == MachineType.InnerFabric || ownType == MachineType.SurfaceFabric) {
			sd.setType(AssemblerType.Fabric.toString());
			sd.setName(AssemblerType.Fabric.toString());
		} else if (ownType == MachineType.Sole || ownType == MachineType.Outsole) {
			sd.setType(AssemblerType.Sole.toString());
			sd.setName(AssemblerType.Sole.toString());
		}
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, dfd);
			if (result.length > 0) {
				return result[0].getName();
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return null;
	}
}
