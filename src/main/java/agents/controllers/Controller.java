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

	private static AID Supervisor = null;

	private static ContainerController AgentContainer = null;

	@GetMapping("/state")
	public ResponseEntity<List<Event>> getState() {
		return new ResponseEntity<>(Event.eventList, HttpStatus.OK);
	}

	@PostMapping("/order")
	public ResponseEntity<?> createOrder(@RequestBody ProductOrder order) {
		createActuator("CreateOrderActuator", "agents.actuators.CreateOrderActuator", new Object[]{Supervisor, JsonConverter.toJsonString(order)});
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/shutdown")
	public ResponseEntity<?> shutdownSystem(){
		createActuator("ShutdownSystemActuator", "agents.actuators.ShutdownSystemActuator", new Object[]{});
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private void createActuator(String name, String className, Object[] actuatorParameters){
		if(Supervisor != null && AgentContainer != null){
			try {
				AgentController ac = AgentContainer.createNewAgent(name, className, actuatorParameters);
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}

	public static void setSupervisor(AID supervisor){ Supervisor = supervisor; }
	public static void setContainerController(ContainerController agentContainer){ AgentContainer = agentContainer; }


}
