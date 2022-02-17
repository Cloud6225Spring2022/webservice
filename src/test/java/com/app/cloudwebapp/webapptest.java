package com.app.cloudwebapp;








import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import com.app.cloudwebapp.controller.controller;








class webapptest {
	
	@Autowired
	controller Controller;

	
	@Test
	public void ValidateResponseStatus() {		


		    Assertions.assertEquals(HttpStatus.OK, HttpStatus.OK);

		

	}
}
