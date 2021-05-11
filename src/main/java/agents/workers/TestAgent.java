package agents.workers;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

public class TestAgent extends Agent {

	@Override
	protected void setup() {

		OneShotBehaviour oneShotBehaviour = new OneShotBehaviour() {
			@Override
			public void action() {
				System.out.println("TestAgent is started...");
			}
		};

		CyclicBehaviour cyclicBehaviour = new CyclicBehaviour() {
			@Override
			public void action() {
				System.out.println("Waiting for some actions...");
				doWait(1000);
			}
		};

		addBehaviour(oneShotBehaviour);
		addBehaviour(cyclicBehaviour);
	}
}

