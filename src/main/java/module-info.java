module agents {
    requires javafx.controls;
    requires javafx.fxml;
	requires jade;
	requires com.fasterxml.jackson.databind;

	exports agents;
	exports agents.product;
	exports agents.managers;
	exports agents.workers.assemblers;
	exports agents.workers.machines;
	exports agents.workers;
	exports agents.utils;

	opens agents.product;
	opens agents to javafx.fxml;
    opens agents.workers to jade;
}