package com.app.cloudwebapp.controller;





import com.amazonaws.util.IOUtils;
import com.app.cloudwebapp.Model.ProfilePic;
import com.app.cloudwebapp.Model.User;
import com.app.cloudwebapp.Repository.UserPicRepository;
import com.app.cloudwebapp.Repository.UserRepository;
import com.app.cloudwebapp.Config.FileUploadProperties;
import com.app.cloudwebapp.Service.FileUploadService;
import com.app.cloudwebapp.Service.UserPicService;
import com.app.cloudwebapp.Validators.UserValidator;
import com.timgroup.statsd.StatsDClient;

import org.apache.tomcat.util.json.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@RequestMapping("/")
@CrossOrigin("*")
public class controller {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserPicRepository userPicRepository;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    FileUploadService fileUploadService;

    @Autowired
    UserPicService userPicService;
    
    @Autowired
    private StatsDClient metric;

    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(userValidator);
    }
    
    Logger logger = LoggerFactory.getLogger(controller.class);


    @GetMapping("/health")
    public ResponseEntity<String> getUsers() {
        logger.info("Successful Health check");
        return ResponseEntity.status(HttpStatus.OK).body("");
        

    }


    @GetMapping("/v1/user/self")
    public ResponseEntity<User> getUserById(Authentication authentication) {
    	
    	metric.incrementCounter("GetUserInfo");
        logger.info("Inside get User Function");

        metric.incrementCounter("apiCall");
        
        if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        logger.info("Validating Authenticatio Header");

        Optional<User> user = userRepository.findByUsername(authentication.getName());
        logger.info("User Found and return data");
        return ResponseEntity.status(HttpStatus.OK).body(user.get());
    }

    @PostMapping("/v1/user")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user, BindingResult errors) throws ParseException {
    	long startTimer = System.currentTimeMillis();
        metric.incrementCounter("CreateUser");
        logger.info("Inside create User Function");
    	
    	List<User> users = userRepository.findAll();
        //System.out.println("New user: " + newUser.toString());
        for (User user2 : users) {
            //System.out.println("Registered user: " + newUser.toString());
            if (user2.getUsername().equals(user.getUsername())) {
            	logger.info("Inside User already exist");
            	// System.out.println("User Already exists!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }
        if (errors.hasErrors()) {


            //String error  = errors;
            System.out.println("error" + errors.getFieldError().getCode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } else {


            String regex = "^(.+)@(.+)$";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(user.getUsername());
            if (!matcher.matches()) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);


            }


            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setAccount_created(new Timestamp(System.currentTimeMillis()));
            user.setAccount_updated(new Timestamp(System.currentTimeMillis()));
            user.setActive(true);

            User createdUser = userRepository.save(user);

            long uploadPictureTimer = System.currentTimeMillis();
            long elapsedTime = uploadPictureTimer - startTimer;
            metric.recordExecutionTime("CreateUserTimer", elapsedTime);


            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        }

    }

    // @PreAuthorize(value = "isAuthenticated()")
    @PutMapping("/v1/user/self")
    public ResponseEntity<User> UpdateUser(Authentication authentication, @Valid @RequestBody User updatedUser) {

    	metric.incrementCounter("UpdateUser");

        logger.info("Inside update User Function");

        if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
        	logger.info("Inside update not found ");
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Optional<User> user = userRepository.findByUsername(authentication.getName());

        if (user != null) {


            User user2 = null;

            userRepository.flush();


            String password = updatedUser.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            updatedUser.setPassword(bCryptPasswordEncoder.encode(password));
            updatedUser.setAccount_updated(new Timestamp(System.currentTimeMillis()));


            userRepository.updateUser(authentication.getName(), updatedUser.getFirst_name(),
                    updatedUser.getLast_name(), updatedUser.getPassword(), (Timestamp) updatedUser.getAccount_updated());
            logger.info("update user completed");
            return (getUserById(authentication));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }


    }

    @GetMapping("/v1/user/self/pic")
    public ResponseEntity<String> getUserPic(Authentication authentication) throws JSONException {

    	metric.incrementCounter("GetUserPic");
        logger.info("Inside Get Pic Function");
    	
    	if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
    		logger.info("User not found");
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }


        Optional<User> user = userRepository.findByUsername(authentication.getName());

        if (user != null) {

            Optional<ProfilePic> profilePic = userPicRepository.findByUser(user.get());
            if(!profilePic.isPresent())
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            String jsonString = new JSONObject().put("file_name",profilePic.get().getFile_name())
                    .put("id",profilePic.get().getId() )
                    .put("url", profilePic.get().getUrl())
                    .put("upload_date",profilePic.get().getUpload_date())
                    .put("user_id",profilePic.get().getUser().getId()).toString();
            
            logger.info("User  found");
            return ResponseEntity.status(HttpStatus.OK).body(jsonString);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    @PostMapping("/v1/user/self/pic")
    public ResponseEntity<String> uploadUserPic(@RequestParam("file") MultipartFile file, Authentication authentication) throws ParseException {
    	long startTimer = System.currentTimeMillis();
        metric.incrementCounter("PostUserPic");
        logger.info("User Pic Upload Function");

    	
    	if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
    		logger.info("User not found");
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please provide proper authentication");
        }


        Optional<User> user = userRepository.findByUsername(authentication.getName());

        if (user != null) {

            String message = "";
            try {

              //  Optional<ProfilePic> picProfilePic = userPicRepository.findByUser(user.get());
               // if (picProfilePic.isPresent()) {
                   // System.out.println(picProfilePic.get().getId());

                    if(file.getContentType().equals("image/jpg") || file.getContentType().equals("image/jpeg")
                            || file.getContentType().equals("image/png")) {



                        ProfilePic profilePic = userPicService.uploadPicture(file, user.get().getUsername(),user.get());

                        long uploadPictureTimer = System.currentTimeMillis();
                        long elapsedTime = uploadPictureTimer - startTimer;
                        metric.recordExecutionTime("uploadPictureTimer", elapsedTime);

                        //fileUploadService.save(file,user.get());
                        message = "Uploaded the file successfully: " + file.getOriginalFilename();
                        String jsonString = new JSONObject().put("file_name",profilePic.getFile_name())
                                .put("id",profilePic.getId() )
                                .put("url", profilePic.getUrl())
                                .put("upload_date",profilePic.getUpload_date())
                                .put("user_id",profilePic.getUser().getId()).toString();

                        logger.info("User pic uploaded");
                        return ResponseEntity.status(HttpStatus.OK).body(jsonString);
                    }
             //   }
                return ResponseEntity.status(HttpStatus.OK).body(message);
            } catch (Exception e) {
                
            	message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            	//message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                logger.info("Exception: " + e);
            	return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

    }

    @DeleteMapping("/v1/user/self/pic")
    public ResponseEntity<String> deleteUserPic(Authentication authentication) throws ParseException {
    	metric.incrementCounter("DeleteUserPic");
        logger.info("Inside Delete User pic function");
    	
    	if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please provide proper authentication");
        }

        Optional<User> user = userRepository.findByUsername(authentication.getName());

        if (user != null) {

            String message = "";
            try {

                Optional<ProfilePic> picProfilePic = userPicRepository.findByUser(user.get());
                if (picProfilePic.isPresent()) {

                    ResponseEntity<Object> responseEntity = userPicService.deletePicture(user.get());
                   // userPicRepository.deleteById(picProfilePic.get().getId());
                    logger.info("User pic deleted");
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
                }

            }
            catch (Exception e) {

                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
            }
        }



        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
    }
}




