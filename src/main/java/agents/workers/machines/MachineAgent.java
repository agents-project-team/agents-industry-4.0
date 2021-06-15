package agents.workers.machines;

import agents.events.Event;
import agents.events.EventType;
import agents.product.PartPlan;
import agents.product.ProductPart;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import agents.workers.Worker;
import agents.workers.assemblers.AssemblerType;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class MachineAgent extends Worker<PartPlan> {

	private PartPlan currentPlan;

	private MachineType machineType;

	@Override
	protected void setup() {
		super.setup();
		machineType = MachineType.getByName(getWorkerType());
		super.addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						if(msg.getProtocol().equals("REG")){
							registerAgent("Machine");
						}else if(msg.getProtocol().equals("ACT")){
							Logger.process(getLocalName() + " replaces broken machine");
							moveToMainContainer();
							registerAgent("Machine");
						}
					} else if (msg.getPerformative() == ACLMessage.REQUEST) {
						PartPlan plan = JsonConverter.fromJsonString(msg.getContent(), PartPlan.class);
						currentPlan = plan;

						Logger.info(getLocalName() + " is creating a part");
						Event.createEvent(new Event(EventType.PLAN_RECEIVED, getAID(), getAgentCurrentContainerName(), currentPlan.toEventString()));

						if(!createProcedure()) return;

						ProductPart createdPart = new ProductPart(plan.getPartType());
						Event.createEvent(new Event(EventType.PART_CREATED, getAID(), getAgentCurrentContainerName(), createdPart.toString()));

						AID receiverAssembler = getCurrentAssembler();
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
		if (machineType == MachineType.DetailFabric || machineType == MachineType.InnerFabric || machineType == MachineType.SurfaceFabric) {
			sd.setType("Assembler"+AssemblerType.Fabric.toString());
			sd.setName("Assembler"+AssemblerType.Fabric.toString());
		} else if (machineType == MachineType.Sole || machineType == MachineType.Outsole) {
			sd.setType("Assembler"+AssemblerType.Sole.toString());
			sd.setName("Assembler"+AssemblerType.Sole.toString());
		}
		return getAIDFromDF(sd);
	}

	public boolean createProcedure(){
		doWait((long) (currentPlan.getSeconds() * 1000));
		boolean broke = breakdownProcess();
		if(broke) return false;
		return true;
	}
}
