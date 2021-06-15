package agents.controllers;

import java.util.List;

import agents.events.Event;
import agents.product.ProductOrder;
import agents.utils.JsonConverter;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Controller {

	private AID supervisor = null;

	private ContainerController agentContainer = null;

	@GetMapping("/state")
	public ResponseEntity<List<Event>> getState() {
		return new ResponseEntity<>(Event.eventList, HttpStatus.OK);
	}

	@PostMapping("/order")
	public ResponseEntity<?> createOrder(@RequestBody ProductOrder order) {
		createAgent("CreateOrderActuator", "agents.actuators.CreateOrderActuator", new Object[]{supervisor, JsonConverter.toJsonString(order)});
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private void createAgent(String name, String className, Object[] actuatorParameters){
		if(supervisor != null && agentContainer != null){
			try {
				AgentController ac = agentContainer.createNewAgent(name, className, actuatorParameters);
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}

	public void setSupervisor(AID supervisor){ this.supervisor = supervisor; }
	public void setContainerName(ContainerController agentContainer){ this.agentContainer = agentContainer; }


}
