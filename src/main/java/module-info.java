module agents {
    requires javafx.controls;
    requires javafx.fxml;
	requires jade;

	opens agents to javafx.fxml;
    exports agents;
}