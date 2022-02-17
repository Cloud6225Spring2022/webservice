package com.app.cloudwebapp.controller;




import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.cloudwebapp.Model.User;

import com.app.cloudwebapp.Repository.UserRepository;
import com.app.cloudwebapp.Validators.UserValidator;





@RestController
@RequestMapping("/")
@CrossOrigin("*")
public class controller {
	
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserValidator userValidator;

    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(userValidator);
    }

	
	@GetMapping("/healthz")
	public ResponseEntity<String> getStatus() {
	    return ResponseEntity.ok()
	            .header("Status okay", "200")
	            .body("Application is working fine!!");
	    }
	
	@GetMapping("/v1/user/self")
    public ResponseEntity<User> getUserById( Authentication authentication)
    {
		if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			}

        Optional<User> user = userRepository.findByUsername(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(user.get());
    }

    @PostMapping("/v1/user")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user, BindingResult errors )
    {
        List<User> users = userRepository.findAll();
        
        for (User user2 : users) {
           
            if (user2.getUsername().equals(user.getUsername())) {
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }
        if(errors.hasErrors())
        {



            //String error  = errors;
            System.out.println("error" + errors.getFieldError().getCode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        else {

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setAccount_created(new Timestamp(System.currentTimeMillis()));
            user.setAccount_updated(new Timestamp(System.currentTimeMillis()));
            user.setActive(true);
            User createdUser = userRepository.save(user);
     

            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        }

    }


    @PutMapping("/v1/user/self")
    public ResponseEntity<User> UpdateUser(Authentication authentication, @Valid @RequestBody User updatedUser)
    {
    	if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    		}

        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (updatedUser.getAccount_created() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (updatedUser.getAccount_updated() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        User user2 = null;
        



            Optional<User> user = userRepository.findByUsername(authentication.getName());
            userRepository.flush();
     

        String password = updatedUser.getPassword();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        updatedUser.setPassword(bCryptPasswordEncoder.encode(password));
        updatedUser.setAccount_updated(new Timestamp(System.currentTimeMillis()));


        userRepository.updateUser(authentication.getName(), updatedUser.getFirst_name(),
                updatedUser.getLast_name(), updatedUser.getPassword(), (Timestamp) updatedUser.getAccount_updated());
        return(getUserById(authentication));


           
    }


}


