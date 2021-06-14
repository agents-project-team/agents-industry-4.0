package agents.controllers;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

	@GetMapping("/state")
	public ResponseEntity<List<Object>> getState() {
		return new ResponseEntity<>(List.of(), HttpStatus.OK);
	}

	@PostMapping("/order")
	public ResponseEntity<?> createOrder(@RequestBody Object order) {
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
