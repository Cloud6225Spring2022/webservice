package com.app.cloudwebapp.controller;




import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;





@RestController
@RequestMapping("/apistat")
@CrossOrigin("*")
public class controller {
	
	@GetMapping("/getstatus")
	public ResponseEntity<String> getStatus() {
	    return ResponseEntity.ok()
	            .header("Status okay", "200")
	            .body("Application is working fine!!");
	    }

}


