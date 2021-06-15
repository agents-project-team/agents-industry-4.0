package agents;

import jade.Boot;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App  {

    public static void main(String[] args) {

//		String[] parameters = {
//				"-gui",
//				"Supervisor:agents.supervisor.SupervisorAgent;"
//		};

		SpringApplication.run(App.class, args);
//		Boot.main(parameters);

		jade.core.Runtime rt = jade.core.Runtime.instance();
		rt.setCloseVM(true);
		Profile profile = new ProfileImpl(null, 1200, null);
		AgentContainer mainContainer = rt.createMainContainer(profile);
		try {
			AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
			rma.start();
			AgentController supervisor = mainContainer.createNewAgent("Supervisor", "agents.supervisor.SupervisorAgent", new Object[0]);
			supervisor.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
    }

}