module agents {
    requires javafx.controls;
    requires javafx.fxml;
	requires jade;
	requires com.fasterxml.jackson.databind;

	exports agents;
	exports agents.product;
	exports agents.managers;
	exports agents.assemblers;
	exports agents.utils;
	exports agents.workers;
	opens agents.product;
	opens agents to javafx.fxml;
    opens agents.workers to jade;
}