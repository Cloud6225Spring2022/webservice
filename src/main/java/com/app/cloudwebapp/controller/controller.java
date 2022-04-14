package com.app.cloudwebapp.controller;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.util.IOUtils;
import com.app.cloudwebapp.Model.Account;
import com.app.cloudwebapp.Model.ProfilePic;
import com.app.cloudwebapp.Model.User;
import com.app.cloudwebapp.Repository.UserPicRepository;
import com.app.cloudwebapp.Repository.UserRepository;
import com.app.cloudwebapp.Config.FileUploadProperties;
import com.app.cloudwebapp.Service.FileUploadService;
import com.app.cloudwebapp.Service.UserPicService;
import com.app.cloudwebapp.Service.UserService;
import com.app.cloudwebapp.Validators.UserValidator;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

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
    UserService userService;

    @Autowired
    private StatsDClient metric;

    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(userValidator);
    }


    Logger logger = LoggerFactory.getLogger(controller.class);

    @GetMapping("/healthzz")
    public ResponseEntity<String> getUsers() {
        return ResponseEntity.status(HttpStatus.OK).body("");

    }
    
     @GetMapping("/testing")
    public ResponseEntity<String> gettest() {
        return ResponseEntity.status(HttpStatus.OK).body("");

    }


    @GetMapping(value = "/v1/verifyUserEmail")
    public ResponseEntity accountVerification(@RequestParam(name="email") String email, @RequestParam(name = "token") String token) {
        metric.incrementCounter("Verify New User API");

        logger.info("Verify New User API called.");
        Optional<User> user = userRepository.findByUsername(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            //throw new ApiException("User not found!!");
        }
        if(!user.get().getActive()) {
            Account account = userService.verifyAccount(email, token);
            if (account != null) {
                User updateUser = user.get();
                Boolean isActive = true;
                // userRepository.updateUser();
                userRepository.updateUser(updateUser.getUsername(), updateUser.getFirst_name(),
                        updateUser.getLast_name(), updateUser.getPassword(), (Timestamp) updateUser.getAccount_updated(),
                        isActive);
                logger.info("update user completed");
                return new ResponseEntity<String>("Your account is verified!!", HttpStatus.OK);
            } else {
                logger.info("Account is null or token expired");
                return new ResponseEntity<String>("Your account is not verified!!, check token is expired or user already verified", HttpStatus.OK);

            }
        }
        else
        {
            logger.info("Account is verified");
            return new ResponseEntity<String>("Your account is already verified!!", HttpStatus.OK);

        }
    }


    @GetMapping("/v1/user/self")
    public ResponseEntity getUserById(Authentication authentication) {

        metric.incrementCounter("GetUserInfo");
        logger.info("Inside get User Function");

        metric.incrementCounter("apiCall");
        if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
            logger.warn("Request header did not get authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);


        }

        logger.info("Validating Authenticatio Header");

        Optional<User> user = userRepository.findByUsername(authentication.getName());
        if(user.get().getActive())
        {
            logger.info("User Found and return data");
            return ResponseEntity.status(HttpStatus.OK).body(user.get());
        }
        else {
            logger.info("User not Active ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not verified");
        }

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
            AmazonSNS snsClient = AmazonSNSClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

            CreateTopicResult topicResult = snsClient.createTopic("email");
            String topicArn = topicResult.getTopicArn();
            logger.info("topicArn "+topicArn.toString());
            final PublishRequest publishRequest = new PublishRequest(topicArn, user.getUsername());
            logger.info("publishRequest  made "+publishRequest.getMessage());
            final PublishResult PublishResult = snsClient.publish(publishRequest);
            logger.info("PublishResult request made "+PublishResult.toString());
            long uploadPictureTimer = System.currentTimeMillis();
            long elapsedTime = uploadPictureTimer - startTimer;
            metric.recordExecutionTime("CreateUserTimer", elapsedTime);


            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        }

    }

    // @PreAuthorize(value = "isAuthenticated()")
    @PutMapping("/v1/user/self")
    public ResponseEntity UpdateUser(Authentication authentication, @Valid @RequestBody User updatedUser) {

        metric.incrementCounter("UpdateUser");

        logger.info("Inside update User Function");
        if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
            logger.info("Inside update not found ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Optional<User> user = userRepository.findByUsername(authentication.getName());

        if (user != null) {

            if(user.get().getActive()) {
                User user2 = null;

                userRepository.flush();


                String password = updatedUser.getPassword();
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                updatedUser.setPassword(bCryptPasswordEncoder.encode(password));
                updatedUser.setAccount_updated(new Timestamp(System.currentTimeMillis()));


                userRepository.updateUser(authentication.getName(), updatedUser.getFirst_name(),
                        updatedUser.getLast_name(), updatedUser.getPassword(), (Timestamp) updatedUser.getAccount_updated(), updatedUser.getActive());
                logger.info("update user completed");
                return (getUserById(authentication));
            }else
            {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not verified");
            }
        } else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }




    @GetMapping("/v1/user/self/pic")
    public ResponseEntity getUserPic(Authentication authentication) throws JSONException {

        metric.incrementCounter("GetUserPic");
        logger.info("Inside Get Pic Function");
        if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
            logger.info("User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }


        Optional<User> user = userRepository.findByUsername(authentication.getName());

        if (user != null) {

            if(user.get().getActive()) {

                Optional<ProfilePic> profilePic = userPicRepository.findByUser(user.get());
                if (!profilePic.isPresent()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
                String jsonString = new JSONObject().put("file_name", profilePic.get().getFile_name())
                        .put("id", profilePic.get().getId())
                        .put("url", profilePic.get().getUrl())
                        .put("upload_date", profilePic.get().getUpload_date())
                        .put("user_id", profilePic.get().getUser().getId()).toString();

                logger.info("User  found");
                return ResponseEntity.status(HttpStatus.OK).body(profilePic.get());
            }
            else {
                logger.info("User  found");
                return ResponseEntity.status(HttpStatus.OK).body("User not verified");
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    @PostMapping("/v1/user/self/pic")
    public ResponseEntity uploadUserPic(@RequestParam("file") MultipartFile file, Authentication authentication) throws ParseException {
        long startTimer = System.currentTimeMillis();
        metric.incrementCounter("PostUserPic");
        logger.info("User Pic Upload Function");
        if (authentication == null || authentication.getName() == null && authentication.getName().isEmpty()) {
            logger.info("User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please provide proper authentication");
        }


        Optional<User> user = userRepository.findByUsername(authentication.getName());

        if (user != null) {

            if(user.get().getActive()) {

                String message = "";
                try {

                    //  Optional<ProfilePic> picProfilePic = userPicRepository.findByUser(user.get());
                    // if (picProfilePic.isPresent()) {
                    // System.out.println(picProfilePic.get().getId());

                    if (file.getContentType().equals("image/jpg") || file.getContentType().equals("image/jpeg")
                            || file.getContentType().equals("image/png")) {


                        ProfilePic profilePic = userPicService.uploadPicture(file, user.get().getUsername(), user.get());


                        long uploadPictureTimer = System.currentTimeMillis();
                        long elapsedTime = uploadPictureTimer - startTimer;
                        metric.recordExecutionTime("uploadPictureTimer", elapsedTime);
                        //fileUploadService.save(file,user.get());
                        message = "Uploaded the file successfully: " + file.getOriginalFilename();
                        String jsonString = new JSONObject().put("file_name", profilePic.getFile_name())
                                .put("id", profilePic.getId())
                                .put("url", profilePic.getUrl())
                                .put("upload_date", profilePic.getUpload_date())
                                .put("user_id", profilePic.getUser().getId()).toString();

                        logger.info("User pic uploaded");
                        return ResponseEntity.status(HttpStatus.OK).body(profilePic);
                    }
                    //   }
                    return ResponseEntity.status(HttpStatus.OK).body(message);
                } catch (Exception e) {
                    message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                    logger.info("Exception: " + e);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
                }
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not verified");
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
            logger.info("User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please provide proper authentication");
        }

        Optional<User> user = userRepository.findByUsername(authentication.getName());

        if (user != null) {

            if (user.get().getActive()) {

                String message = "";
                try {

                    Optional<ProfilePic> picProfilePic = userPicRepository.findByUser(user.get());
                    if (picProfilePic.isPresent()) {

                        ResponseEntity<Object> responseEntity = userPicService.deletePicture(user.get());
                        // userPicRepository.deleteById(picProfilePic.get().getId());
                        logger.info("User pic deleted");
                        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
                    }

                } catch (Exception e) {

                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
                }
            }else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not verified");
            }
        }



        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
    }
}




