package agents;

import jade.Boot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App  {

    public static void main(String[] args) {

		String[] parameters = {
				"-gui",
				"Supervisor:agents.supervisor.SupervisorAgent;"
		};

		SpringApplication.run(App.class, args);
		Boot.main(parameters);

    }

}